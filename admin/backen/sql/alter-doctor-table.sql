-- ------------------------------------------------------------
-- 管理员端医生账号管理模块：数据库变更脚本
-- 变更点：为 doctor 表补充 gender、hire_date 字段
-- ------------------------------------------------------------

ALTER TABLE doctor
  ADD COLUMN gender VARCHAR(10) NULL COMMENT '性别' AFTER specialty,
  ADD COLUMN hire_date DATE NULL COMMENT '入职日期' AFTER gender;

UPDATE doctor SET gender = '男' WHERE gender IS NULL;