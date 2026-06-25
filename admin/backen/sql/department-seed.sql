USE doctor_platform;

-- ============================================================
-- 科室测试数据（department-seed）
-- 说明：
--   1. 只增不减，使用 dept_code 做唯一判断 (ON DUPLICATE KEY UPDATE)
--   2. 覆盖新字段：parent_id / floor / phone / sort_order
--   3. 层级结构（三级）：
--        第一层：门诊部 / 住院部 / 医技部 / 急诊科 / 已停诊科室
--        第二层：神经内科 / 心血管内科 / 全科门诊 / 儿科 /
--                内科病房 / 外科病房 / 放射科 / 检验科 / 超声科 /
--                急诊内科 / 急诊外科
--        第三层：头痛门诊 / 癫痫门诊 / 卒中门诊 /
--                高血压门诊 / 冠心病门诊 / 心衰门诊
--   4. 包含 1 个停用科室，测试状态过滤
--   5. 含关联医生（doctor.dept_id）和排班（doctor_schedule.dept_id），
--      验证"科室下有医生/排班则不能删除"
-- 执行顺序：schema.sql → change.sql → department-seed.sql → seed-data.sql
-- ============================================================

-- 临时关闭外键检查，避免同语句内逐行检查导致 1452 报错
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 第一层：顶层大科室（parent_id = NULL）
-- ------------------------------------------------------------
INSERT INTO department (
  dept_id, dept_code, dept_name, dept_type, parent_id, floor, phone, location, description, status, sort_order
) VALUES
  (1, 'OUTPATIENT_DEPT', '门诊部',     '临床科室', NULL, '门诊楼一层', '010-88881001', '门诊楼一层导诊台', '医院门诊业务统一管理中心',                    1, 1),
  (2, 'INPATIENT_DEPT',  '住院部',     '临床科室', NULL, '住院楼一层', '010-88881002', '住院楼一层登记处',   '住院病人入院、转科、出院管理',                1, 2),
  (3, 'MED_TECH_DEPT',   '医技部',     '医技科室', NULL, '医技楼一层', '010-88881003', '医技楼一层大厅',     '放射、检验、B超等医技科室统一管理',            1, 3),
  (4, 'EMERGENCY_DEPT',  '急诊科',     '临床科室', NULL, '急诊楼一层', '010-88881004', '急诊楼一层分诊台',   '24小时急诊绿色通道',                          1, 4),
  (5, 'DISABLED_DEPT',   '已停诊科室', '其他',     NULL, '住院楼五层', '010-88881005', '住院楼五层西侧',     '临时停诊的科室示例（测试 status=0 过滤）',     0, 99)
ON DUPLICATE KEY UPDATE
  dept_name   = VALUES(dept_name),
  dept_type   = VALUES(dept_type),
  parent_id   = VALUES(parent_id),
  floor       = VALUES(floor),
  phone       = VALUES(phone),
  location    = VALUES(location),
  description = VALUES(description),
  status      = VALUES(status),
  sort_order  = VALUES(sort_order);

-- ------------------------------------------------------------
-- 第二层：临床科室 / 医技科室（归属各顶层大科室）
-- ------------------------------------------------------------
INSERT INTO department (
  dept_id, dept_code, dept_name, dept_type, parent_id, floor, phone, location, description, status, sort_order
) VALUES
  -- 门诊部下属 (parent_id = 1)
  (10, 'NEUROLOGY',     '神经内科',   '临床科室', 1, '门诊楼二层', '010-88882001', '门诊楼二层北区', '头痛、眩晕、脑血管疾病、癫痫、周围神经病等', 1, 1),
  (11, 'CARDIOLOGY',    '心血管内科', '临床科室', 1, '门诊楼三层', '010-88882002', '门诊楼三层北区', '高血压、冠心病、心律失常、心力衰竭门诊',      1, 2),
  (12, 'GENERAL',       '全科门诊',   '临床科室', 1, '门诊楼一层', '010-88882003', '门诊楼一层东侧', '常见病、慢病管理、健康咨询',                  1, 3),
  (13, 'PEDIATRICS',    '儿科',       '临床科室', 1, '门诊楼四层', '010-88882004', '门诊楼四层东区', '儿童常见病、儿童保健',                        1, 4),

  -- 住院部下属 (parent_id = 2)
  (20, 'INTERNAL_WARD', '内科病房',   '临床科室', 2, '住院楼三层', '010-88883001', '住院楼三层',     '内科综合病房',                                1, 1),
  (21, 'SURGERY_WARD',  '外科病房',   '临床科室', 2, '住院楼四层', '010-88883002', '住院楼四层',     '外科综合病房',                                1, 2),

  -- 医技部下属 (parent_id = 3)
  (30, 'RADIOLOGY',     '放射科',     '医技科室', 3, '医技楼一层', '010-88884001', '医技楼一层东侧', 'CT、MRI、X线、DR',                            1, 1),
  (31, 'LABORATORY',    '检验科',     '医技科室', 3, '医技楼二层', '010-88884002', '医技楼二层',     '生化、免疫、血常规、微生物',                  1, 2),
  (32, 'ULTRASOUND',    '超声科',     '医技科室', 3, '医技楼三层', '010-88884003', '医技楼三层',     '腹部、心脏、血管超声',                        1, 3),

  -- 急诊科下属 (parent_id = 4)
  (40, 'EMERGENCY_INT', '急诊内科',   '临床科室', 4, '急诊楼一层', '010-88885001', '急诊楼一层北区', '急性胸痛、卒中、呼吸困难等急诊内科',           1, 1),
  (41, 'EMERGENCY_SUR', '急诊外科',   '临床科室', 4, '急诊楼一层', '010-88885002', '急诊楼一层南区', '外伤、急腹症等急诊外科',                      1, 2)
ON DUPLICATE KEY UPDATE
  dept_name   = VALUES(dept_name),
  dept_type   = VALUES(dept_type),
  parent_id   = VALUES(parent_id),
  floor       = VALUES(floor),
  phone       = VALUES(phone),
  location    = VALUES(location),
  description = VALUES(description),
  status      = VALUES(status),
  sort_order  = VALUES(sort_order);

-- ------------------------------------------------------------
-- 第三层：亚专业科室（parent_id 指向第二层，树形结构测试）
-- ------------------------------------------------------------
INSERT INTO department (
  dept_id, dept_code, dept_name, dept_type, parent_id, floor, phone, location, description, status, sort_order
) VALUES
  -- 神经内科子科室 (parent_id = 10)
  (100, 'HEADACHE_CLINIC', '头痛门诊',   '临床科室', 10, '门诊楼二层', '010-88886001', '门诊楼二层201室', '原发性头痛、偏头痛、紧张性头痛门诊',       1, 1),
  (101, 'EPILEPSY_CLINIC', '癫痫门诊',   '临床科室', 10, '门诊楼二层', '010-88886002', '门诊楼二层202室', '癫痫诊断、长期随访',                       1, 2),
  (102, 'STROKE_CLINIC',   '卒中门诊',   '临床科室', 10, '门诊楼二层', '010-88886003', '门诊楼二层203室', '缺血性/出血性卒中筛查与随访',               1, 3),

  -- 心血管内科子科室 (parent_id = 11)
  (110, 'HTN_CLINIC',      '高血压门诊', '临床科室', 11, '门诊楼三层', '010-88886004', '门诊楼三层301室', '原发性/继发性高血压门诊',                   1, 1),
  (111, 'CHD_CLINIC',      '冠心病门诊', '临床科室', 11, '门诊楼三层', '010-88886005', '门诊楼三层302室', '冠脉疾病、支架后随访',                       1, 2),
  (112, 'HEART_FAILURE',   '心衰门诊',   '临床科室', 11, '门诊楼三层', '010-88886006', '门诊楼三层303室', '慢性心力衰竭综合管理',                       1, 3)
ON DUPLICATE KEY UPDATE
  dept_name   = VALUES(dept_name),
  dept_type   = VALUES(dept_type),
  parent_id   = VALUES(parent_id),
  floor       = VALUES(floor),
  phone       = VALUES(phone),
  location    = VALUES(location),
  description = VALUES(description),
  status      = VALUES(status),
  sort_order  = VALUES(sort_order);

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 结束（共 21 个科室：5 顶层 + 11 二层 + 6 三层 + 1 停用）
-- 建议后续执行：seed-data.sql（补医生、排班、挂号等）
-- ============================================================