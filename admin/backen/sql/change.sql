-- ============================================================
-- change.sql
-- 数据库增量变更脚本（只增不减 & 幂等可重复执行）
-- 执行顺序：schema.sql → change.sql → seed-data.sql
-- 说明：所有 ADD COLUMN 先检查 information_schema，
--       已存在则跳过；所有 UPDATE/MODIFY 不受列存在性影响，
--       保证 change.sql 可重复运行而不报错。
-- ============================================================

USE doctor_platform;

-- ------------------------------------------------------------
-- 工具过程：安全地为某表增加一列（列已存在则跳过）
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DELIMITER $$
CREATE PROCEDURE add_column_if_not_exists(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_def TEXT
)
BEGIN
    DECLARE col_exists INT;
    SELECT COUNT(*) INTO col_exists
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name;
    IF col_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN ', p_column_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$
DELIMITER ;

-- ------------------------------------------------------------
-- 工具过程：安全地为某表增加外键（约束已存在则跳过）
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_fk_if_not_exists;
DELIMITER $$
CREATE PROCEDURE add_fk_if_not_exists(
    IN p_table_name VARCHAR(64),
    IN p_constraint_name VARCHAR(64),
    IN p_fk_def TEXT
)
BEGIN
    DECLARE fk_exists INT;
    SELECT COUNT(*) INTO fk_exists
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND CONSTRAINT_NAME = p_constraint_name;
    IF fk_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' ', p_fk_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$
DELIMITER ;

-- ------------------------------------------------------------
-- 改动 1：doctor 表移除冗余的 doctor_name 字段
-- 原因：doctor 表通过 user_id 关联 sys_user，医生姓名应当由 sys_user.real_name
--       提供，冗余字段会导致数据不一致（新增/修改医生时双写容易漏），且违反范式。
--       原有数据（doctor_name）已无业务用途，可安全丢弃；新建表直接不包含此字段。
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS drop_column_if_exists;
DELIMITER $$
CREATE PROCEDURE drop_column_if_exists(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64)
)
BEGIN
    DECLARE col_exists INT;
    SELECT COUNT(*) INTO col_exists
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name;
    IF col_exists = 1 THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP COLUMN ', p_column_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$
DELIMITER ;

CALL drop_column_if_exists('doctor', 'doctor_name');

-- ------------------------------------------------------------
-- 改动 2：doctor_schedule.status 从中文值迁移为英文枚举值
-- 原因：后端 Java 代码使用 "AVAILABLE" / "CANCELLED"，
--       前端使用 "AVAILABLE" / "FULL" / "CANCELLED"，
--       但数据库默认值是中文 '可预约'，导致前后端匹配不上。
-- ------------------------------------------------------------

-- 先将现有中文值迁移为对应英文枚举
UPDATE doctor_schedule SET status = 'AVAILABLE' WHERE status = '可预约';
UPDATE doctor_schedule SET status = 'CANCELLED' WHERE status IN ('已取消', '停诊');

-- 改变列的默认值为英文枚举
ALTER TABLE doctor_schedule
  MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE';

-- ------------------------------------------------------------
-- 改动 3：doctor_schedule 增加 source 列（排班来源）
-- 来源：schedule-ai-migration.sql
-- 原因：AI 排班建议功能需要区分排班是人工创建还是 AI 推荐，
--       用于前端展示不同的标识图标和操作入口。
--       枚举值：MANUAL / AI_SUGGESTED
-- ------------------------------------------------------------
CALL add_column_if_not_exists('doctor_schedule', 'source',
    'source VARCHAR(30) NOT NULL DEFAULT ''MANUAL'' AFTER status');

-- ------------------------------------------------------------
-- 改动 4：创建 ai_schedule_suggestion_detail 表（AI 排班建议详情）
-- 来源：schedule-ai-migration.sql
-- 原因：一条 AI 排班建议可能包含多条建议明细（不同医生/不同时段），
--       需要独立表来承载这些详情，供管理员逐条确认或忽略。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_schedule_suggestion_detail (
  detail_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  suggestion_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  doctor_name VARCHAR(50) NOT NULL,
  schedule_date DATE NOT NULL,
  time_slot VARCHAR(20) NOT NULL,
  max_appointments INT NOT NULL,
  reason TEXT,
  CONSTRAINT fk_ai_schedule_detail_suggestion FOREIGN KEY (suggestion_id) REFERENCES ai_schedule_suggestion(suggestion_id),
  CONSTRAINT fk_ai_schedule_detail_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 改动 5：ai_schedule_suggestion_detail 增加 status 列
-- 来源：ai-suggestion-detail-status-migration.sql
-- 原因：每一条 AI 建议明细需要有独立的处理状态（待确认/已采纳/已忽略），
--       否则管理员无法逐条操作。与前端 Schedule.vue 中的建议弹窗状态联动。
--       枚举值：PENDING / ACCEPTED / REJECTED
-- ------------------------------------------------------------
CALL add_column_if_not_exists('ai_schedule_suggestion_detail', 'status',
    'status VARCHAR(20) NOT NULL DEFAULT ''PENDING'' AFTER reason');

-- ------------------------------------------------------------
-- 改动 6：department 表补充树形结构与基础信息字段
-- 原因：科室管理模块需要支持多级科室（parent_id）、楼层与电话展示（floor/phone）、
--       以及显式排序（sort_order）。原有字段（dept_id / dept_code / dept_name /
--       dept_type / location / description / status / created_at / updated_at）
--       保持不变，仅追加新列。
-- ------------------------------------------------------------
CALL add_column_if_not_exists('department', 'parent_id',
    'parent_id BIGINT NULL AFTER dept_id');
CALL add_column_if_not_exists('department', 'floor',
    'floor VARCHAR(50) NULL AFTER dept_type');
CALL add_column_if_not_exists('department', 'phone',
    'phone VARCHAR(30) NULL AFTER floor');
CALL add_column_if_not_exists('department', 'sort_order',
    'sort_order INT NOT NULL DEFAULT 0 AFTER status');

CALL add_fk_if_not_exists('department', 'fk_department_parent',
    'ADD CONSTRAINT fk_department_parent FOREIGN KEY (parent_id) REFERENCES department(dept_id)');

-- ------------------------------------------------------------
-- 清理：删除临时存储过程
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS add_fk_if_not_exists;

-- ============================================================
-- 结束
-- ============================================================