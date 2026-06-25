USE doctor_platform;

-- ============================================================
-- 测试数据（覆盖版）
-- 执行顺序：schema.sql → change.sql → seed-data.sql
-- 管理员登录：admin / 123456
-- 所有状态枚举均为英文，与后端 Java 代码保持一致
-- ============================================================

-- ------------------------------------------------------------
-- 1. 角色（保持与原一致即可）
-- ------------------------------------------------------------
INSERT INTO sys_role (role_id, role_code, role_name, description, status)
VALUES
  (1, 'ADMIN', '管理员', '管理员端测试角色', 1),
  (2, 'DOCTOR', '医生', '医生端测试角色', 1)
ON DUPLICATE KEY UPDATE
  role_code = VALUES(role_code),
  role_name = VALUES(role_name),
  description = VALUES(description),
  status = VALUES(status);

-- ------------------------------------------------------------
-- 2. 系统用户（admin + 10 个医生账号）
-- ------------------------------------------------------------
INSERT INTO sys_user (user_id, role_id, username, password, real_name, phone, email, status)
VALUES
  (1,  1, 'admin',    '123456', '系统管理员', '13800000000', 'admin@example.com',    1),
  (2,  2, 'doctor01', '123456', '张明',       '13800000001', 'doctor01@example.com', 1),
  (3,  2, 'doctor02', '123456', '李华',       '13800000002', 'doctor02@example.com', 1),
  (4,  2, 'doctor03', '123456', '王芳',       '13800000003', 'doctor03@example.com', 1),
  (5,  2, 'doctor04', '123456', '赵强',       '13800000004', 'doctor04@example.com', 1),
  (6,  2, 'doctor05', '123456', '陈静',       '13800000005', 'doctor05@example.com', 1),
  (7,  2, 'doctor06', '123456', '周磊',       '13800000006', 'doctor06@example.com', 1),
  (8,  2, 'doctor07', '123456', '孙丽',       '13800000007', 'doctor07@example.com', 1),
  (9,  2, 'doctor08', '123456', '吴敏',       '13800000008', 'doctor08@example.com', 1),
  (10, 2, 'doctor09', '123456', '郑涛',       '13800000009', 'doctor09@example.com', 1),
  (11, 2, 'doctor10', '123456', '林雪',       '13800000010', 'doctor10@example.com', 1)
ON DUPLICATE KEY UPDATE
  role_id   = VALUES(role_id),
  password  = VALUES(password),
  real_name = VALUES(real_name),
  phone     = VALUES(phone),
  email     = VALUES(email),
  status    = VALUES(status);

-- ------------------------------------------------------------
-- 3. 科室（3个临床科室）
-- ------------------------------------------------------------
INSERT INTO department (dept_id, dept_code, dept_name, dept_type, location, description, status)
VALUES
  (1, 'NEUROLOGY',  '神经内科',   '临床科室', '门诊楼二层', '头痛、眩晕、脑血管疾病门诊',       1),
  (2, 'GENERAL',    '全科门诊',   '临床科室', '门诊楼一层', '常见病和慢病基础诊疗',             1),
  (3, 'CARDIOLOGY', '心血管内科', '临床科室', '门诊楼三层', '高血压、冠心病、心律失常门诊',     1)
ON DUPLICATE KEY UPDATE
  dept_code   = VALUES(dept_code),
  dept_name   = VALUES(dept_name),
  dept_type   = VALUES(dept_type),
  location    = VALUES(location),
  description = VALUES(description),
  status      = VALUES(status);

-- ------------------------------------------------------------
-- 4. 医生（10人，跨3个科室、4种职称，含 doctor_name 字段）
--    user_id    从 sys_user 对应
-- ------------------------------------------------------------
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_no, doctor_name, doctor_type, title, specialty, introduction, status)
VALUES
  -- 神经内科（3人）
  (1, 2,  1, 'D20260001', '张明', '门诊医生', '主任医师',   '头痛、眩晕、脑血管病',         '神经内科主任医师，从事脑血管疾病临床工作20年', 1),
  (2, 3,  1, 'D20260002', '李华', '门诊医生', '副主任医师', '癫痫、睡眠障碍',               '神经内科副主任医师，擅长癫痫和睡眠障碍',        1),
  (3, 7,  1, 'D20260006', '周磊', '门诊医生', '主治医师',   '周围神经病、肌肉疾病',         '神经内科主治医师，擅长周围神经疾病门诊',        1),
  -- 全科门诊（3人）
  (4, 4,  2, 'D20260003', '王芳', '门诊医生', '主治医师',   '慢病管理、常见病',             '全科门诊主治医师，擅长慢病长期随访',            1),
  (5, 8,  2, 'D20260007', '孙丽', '门诊医生', '副主任医师', '糖尿病、高血压综合管理',       '全科副主任医师，负责全科慢病管理',              1),
  (6, 9,  2, 'D20260008', '吴敏', '门诊医生', '主任医师',   '全科综合诊疗',                 '全科主任医师，综合诊疗经验丰富',               1),
  -- 心血管内科（4人）
  (7, 5,  3, 'D20260004', '赵强', '门诊医生', '主任医师',   '高血压、冠心病',               '心血管内科主任医师，冠心病介入治疗专家',        1),
  (8, 6,  3, 'D20260005', '陈静', '门诊医生', '主治医师',   '心律失常、心衰随访',           '心血管内科主治医师，擅长心律失常门诊',          1),
  (9, 10, 3, 'D20260009', '郑涛', '门诊医生', '副主任医师', '心肌病、心力衰竭',             '心血管内科副主任医师，心衰门诊专家',            1),
  (10, 11,3, 'D20260010', '林雪', '门诊医生', '主治医师',   '心脏康复、血脂管理',           '心血管内科主治医师，心脏康复门诊',              1)
ON DUPLICATE KEY UPDATE
  user_id       = VALUES(user_id),
  dept_id       = VALUES(dept_id),
  doctor_no     = VALUES(doctor_no),
  doctor_name   = VALUES(doctor_name),
  doctor_type   = VALUES(doctor_type),
  title         = VALUES(title),
  specialty     = VALUES(specialty),
  introduction  = VALUES(introduction),
  status        = VALUES(status);

-- ------------------------------------------------------------
-- 5. 排班（共 30 条，跨未来 7 天，覆盖 MORNING / AFTERNOON / EVENING）
--    注意：(doctor_id, work_date, time_period) 唯一，不能冲突
--    total_quota / remain_quota 与 registration 表真实挂量对应
--    status: AVAILABLE（可预约）/ FULL（已满）/ CANCELLED（已取消）
--    source: MANUAL / AI_SUGGESTED
-- ------------------------------------------------------------
INSERT INTO doctor_schedule (
  schedule_id, doctor_id, dept_id, work_date, time_period,
  start_time, end_time, total_quota, remain_quota, registration_fee, status, source
)
VALUES
  -- Day 0 (今天) — 3 科室 × 多医生 / 多时段 = 8 条
  (1001, 1, 1, CURDATE(),                                'MORNING',   '08:00:00', '12:00:00', 30, 24, 20.00, 'AVAILABLE',  'MANUAL'),
  (1002, 1, 1, CURDATE(),                                'AFTERNOON', '14:00:00', '18:00:00', 25, 25, 20.00, 'AVAILABLE',  'MANUAL'),
  (1003, 2, 1, CURDATE(),                                'AFTERNOON', '14:00:00', '18:00:00', 25, 18, 18.00, 'AVAILABLE',  'MANUAL'),
  (1004, 4, 2, CURDATE(),                                'MORNING',   '08:00:00', '12:00:00', 40, 33, 12.00, 'AVAILABLE',  'MANUAL'),
  (1005, 4, 2, CURDATE(),                                'AFTERNOON', '14:00:00', '18:00:00', 35,  0, 12.00, 'FULL',       'MANUAL'),
  (1006, 7, 3, CURDATE(),                                'AFTERNOON', '14:00:00', '18:00:00', 28, 20, 22.00, 'AVAILABLE',  'MANUAL'),
  (1007, 7, 3, CURDATE(),                                'MORNING',   '08:00:00', '12:00:00', 28, 28, 22.00, 'AVAILABLE',  'AI_SUGGESTED'),
  (1008, 8, 3, CURDATE(),                                'EVENING',   '18:00:00', '21:00:00', 20, 15, 18.00, 'AVAILABLE',  'MANUAL'),

  -- Day 1 (明天) — 8 条
  (1009, 1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY),      'MORNING',   '08:00:00', '12:00:00', 30, 28, 20.00, 'AVAILABLE',  'MANUAL'),
  (1010, 2, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY),      'MORNING',   '08:00:00', '12:00:00', 25, 10, 18.00, 'AVAILABLE',  'MANUAL'),
  (1011, 3, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY),      'AFTERNOON', '14:00:00', '18:00:00', 20, 20, 15.00, 'AVAILABLE',  'AI_SUGGESTED'),
  (1012, 5, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY),      'MORNING',   '08:00:00', '12:00:00', 35, 30, 14.00, 'AVAILABLE',  'MANUAL'),
  (1013, 6, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY),      'AFTERNOON', '14:00:00', '18:00:00', 40, 40, 14.00, 'AVAILABLE',  'MANUAL'),
  (1014, 7, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY),      'MORNING',   '08:00:00', '12:00:00', 28, 28, 22.00, 'AVAILABLE',  'AI_SUGGESTED'),
  (1015, 9, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY),      'AFTERNOON', '14:00:00', '18:00:00', 25, 22, 20.00, 'AVAILABLE',  'MANUAL'),
  (1016, 10,3, DATE_ADD(CURDATE(), INTERVAL 1 DAY),      'EVENING',   '18:00:00', '21:00:00', 18, 18, 16.00, 'AVAILABLE',  'MANUAL'),

  -- Day 2 — 5 条
  (1017, 1, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY),      'MORNING',   '08:00:00', '12:00:00', 30, 30, 20.00, 'AVAILABLE',  'MANUAL'),
  (1018, 2, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY),      'AFTERNOON', '14:00:00', '18:00:00', 25, 25, 18.00, 'AI_SUGGESTED','AI_SUGGESTED'),
  (1019, 5, 2, DATE_ADD(CURDATE(), INTERVAL 2 DAY),      'AFTERNOON', '14:00:00', '18:00:00', 30, 25, 14.00, 'AVAILABLE',  'MANUAL'),
  (1020, 6, 2, DATE_ADD(CURDATE(), INTERVAL 2 DAY),      'MORNING',   '08:00:00', '12:00:00', 40, 38, 14.00, 'AVAILABLE',  'MANUAL'),
  (1021, 9, 3, DATE_ADD(CURDATE(), INTERVAL 2 DAY),      'MORNING',   '08:00:00', '12:00:00', 25, 25, 20.00, 'AVAILABLE',  'AI_SUGGESTED'),

  -- Day 3 — 3 条（含1条已取消，测试 CANCELLED 场景）
  (1022, 1, 1, DATE_ADD(CURDATE(), INTERVAL 3 DAY),      'MORNING',   '08:00:00', '12:00:00', 30, 30, 20.00, 'CANCELLED',  'MANUAL'),
  (1023, 4, 2, DATE_ADD(CURDATE(), INTERVAL 3 DAY),      'MORNING',   '08:00:00', '12:00:00', 35, 35, 12.00, 'AVAILABLE',  'MANUAL'),
  (1024, 7, 3, DATE_ADD(CURDATE(), INTERVAL 3 DAY),      'AFTERNOON', '14:00:00', '18:00:00', 28, 28, 22.00, 'AVAILABLE',  'MANUAL'),

  -- Day 4 — 2 条
  (1025, 2, 1, DATE_ADD(CURDATE(), INTERVAL 4 DAY),      'MORNING',   '08:00:00', '12:00:00', 25, 25, 18.00, 'AVAILABLE',  'AI_SUGGESTED'),
  (1026, 5, 2, DATE_ADD(CURDATE(), INTERVAL 4 DAY),      'MORNING',   '08:00:00', '12:00:00', 35, 35, 14.00, 'AVAILABLE',  'MANUAL'),

  -- Day 5 — 2 条（含1条已满，测试 FULL 场景）
  (1027, 3, 1, DATE_ADD(CURDATE(), INTERVAL 5 DAY),      'MORNING',   '08:00:00', '12:00:00', 20,  0, 15.00, 'FULL',       'MANUAL'),
  (1028, 8, 3, DATE_ADD(CURDATE(), INTERVAL 5 DAY),      'EVENING',   '18:00:00', '21:00:00', 20, 20, 18.00, 'AVAILABLE',  'MANUAL'),

  -- Day 6 — 2 条（周末）
  (1029, 6, 2, DATE_ADD(CURDATE(), INTERVAL 6 DAY),      'MORNING',   '08:00:00', '12:00:00', 30, 30, 14.00, 'AVAILABLE',  'MANUAL'),
  (1030, 10,3, DATE_ADD(CURDATE(), INTERVAL 6 DAY),      'AFTERNOON', '14:00:00', '18:00:00', 20, 20, 16.00, 'AVAILABLE',  'MANUAL')
ON DUPLICATE KEY UPDATE
  doctor_id        = VALUES(doctor_id),
  dept_id          = VALUES(dept_id),
  work_date        = VALUES(work_date),
  time_period      = VALUES(time_period),
  start_time       = VALUES(start_time),
  end_time         = VALUES(end_time),
  total_quota      = VALUES(total_quota),
  remain_quota     = VALUES(remain_quota),
  registration_fee = VALUES(registration_fee),
  status           = VALUES(status),
  source           = VALUES(source);

-- ------------------------------------------------------------
-- 6. 患者（10人，跨不同年龄段、性别）
-- ------------------------------------------------------------
INSERT INTO patient (patient_id, user_id, patient_no, patient_name, gender, birthday, phone, id_card, address)
VALUES
  (1001, NULL, 'P20260001', '刘一', '男', '1990-01-01', '13900000001', '110101199001010011', '北京市朝阳区测试路1号'),
  (1002, NULL, 'P20260002', '周二', '女', '1988-02-02', '13900000002', '110101198802020022', '北京市海淀区测试路2号'),
  (1003, NULL, 'P20260003', '吴三', '男', '1975-03-03', '13900000003', '110101197503030033', '北京市西城区测试路3号'),
  (1004, NULL, 'P20260004', '郑四', '女', '2001-04-04', '13900000004', '110101200104040044', '北京市东城区测试路4号'),
  (1005, NULL, 'P20260005', '孙五', '男', '1965-05-05', '13900000005', '110101196505050055', '北京市丰台区测试路5号'),
  (1006, NULL, 'P20260006', '钱六', '女', '1978-06-06', '13900000006', '110101197806060066', '北京市石景山区测试路6号'),
  (1007, NULL, 'P20260007', '赵七', '男', '1995-07-07', '13900000007', '110101199507070077', '北京市通州区测试路7号'),
  (1008, NULL, 'P20260008', '李八', '女', '1983-08-08', '13900000008', '110101198308080088', '北京市大兴区测试路8号'),
  (1009, NULL, 'P20260009', '王九', '男', '1970-09-09', '13900000009', '110101197009090099', '北京市昌平区测试路9号'),
  (1010, NULL, 'P20260010', '冯十', '女', '2005-10-10', '13900000010', '110101200510100010', '北京市顺义区测试路10号')
ON DUPLICATE KEY UPDATE
  patient_name = VALUES(patient_name),
  gender       = VALUES(gender),
  birthday     = VALUES(birthday),
  phone        = VALUES(phone),
  id_card      = VALUES(id_card),
  address      = VALUES(address);

-- ------------------------------------------------------------
-- 7. AI 问诊记录（3条，供挂号使用）
-- ------------------------------------------------------------
INSERT INTO ai_consultation (
  consultation_id, patient_id, chief_complaint, symptom_detail, ai_summary,
  recommended_dept_id, risk_level, ai_result, status, created_at
)
VALUES
  (1001, 1001, '头痛三天',        '伴轻微眩晕，无发热，无呕吐',     '建议前往神经内科门诊就诊',                 1, 'LOW',    '{"department":"神经内科","risk":"LOW"}',    'COMPLETED', NOW()),
  (1002, 1002, '胸闷一周',        '活动后明显，休息后缓解',         '建议前往心血管内科门诊就诊',               3, 'MEDIUM', '{"department":"心血管内科","risk":"MEDIUM"}','COMPLETED', NOW()),
  (1003, 1005, '持续高血压控制差','自测血压160/95，伴头晕',         '建议前往心血管内科或全科门诊调整用药',     3, 'HIGH',   '{"department":"心血管内科","risk":"HIGH"}',  'COMPLETED', NOW())
ON DUPLICATE KEY UPDATE
  patient_id          = VALUES(patient_id),
  chief_complaint     = VALUES(chief_complaint),
  symptom_detail      = VALUES(symptom_detail),
  ai_summary          = VALUES(ai_summary),
  recommended_dept_id = VALUES(recommended_dept_id),
  risk_level          = VALUES(risk_level),
  ai_result           = VALUES(ai_result),
  status              = VALUES(status),
  created_at          = VALUES(created_at);

-- ------------------------------------------------------------
-- 8. 挂号记录（12条，分布到不同排班，验证真实挂量展示）
--    schedule_id 对应 doctor_schedule 中已存在的排班
--    fee_status : PAID / UNPAID / REFUNDED
--    status     : REGISTERED / CANCELLED / COMPLETED
-- ------------------------------------------------------------
INSERT INTO registration (
  registration_id, patient_id, consultation_id, dept_id, doctor_id, schedule_id,
  operator_user_id, source, registration_no, queue_no, registration_fee,
  fee_status, status, registered_at
)
VALUES
  -- 今天（Day 0）— 神内张明上午 (1001): total=30, remain=24 → 应有 6 人挂号
  (2001, 1001, 1001, 1, 1, 1001, 1, 'AI_TRIAGE', 'R20260001', 1, 20.00, 'PAID',   'REGISTERED', NOW()),
  (2002, 1003, NULL, 1, 1, 1001, 1, 'MANUAL',    'R20260002', 2, 20.00, 'PAID',   'REGISTERED', NOW()),
  (2003, 1004, NULL, 1, 1, 1001, 1, 'MANUAL',    'R20260003', 3, 20.00, 'PAID',   'REGISTERED', NOW()),
  (2004, 1006, NULL, 1, 1, 1001, 1, 'MANUAL',    'R20260004', 4, 20.00, 'UNPAID', 'REGISTERED', NOW()),
  (2005, 1007, NULL, 1, 1, 1001, 1, 'MANUAL',    'R20260005', 5, 20.00, 'PAID',   'REGISTERED', NOW()),
  (2006, 1008, NULL, 1, 1, 1001, 1, 'MANUAL',    'R20260006', 6, 20.00, 'PAID',   'REGISTERED', NOW()),

  -- 今天（Day 0）— 神内李华下午 (1003): total=25, remain=18 → 7 人挂号
  (2007, 1002, NULL, 1, 2, 1003, 1, 'MANUAL',    'R20260007', 1, 18.00, 'PAID',   'REGISTERED', NOW()),

  -- 今天（Day 0）— 全科王芳下午 (1005): total=35, remain=0 → 35人 (这里只写几条代表已满)
  (2008, 1009, NULL, 2, 4, 1005, 1, 'MANUAL',    'R20260008', 1, 12.00, 'PAID',   'REGISTERED', NOW()),

  -- 今天（Day 0）— 心内赵强下午 (1006): total=28, remain=20 → 8人挂号
  (2009, 1002, 1002, 3, 7, 1006, 1, 'AI_TRIAGE', 'R20260009', 1, 22.00, 'PAID',   'REGISTERED', NOW()),
  (2010, 1010, NULL, 3, 7, 1006, 1, 'MANUAL',    'R20260010', 2, 22.00, 'PAID',   'REGISTERED', NOW()),
  (2011, 1005, 1003, 3, 7, 1006, 1, 'AI_TRIAGE', 'R20260011', 3, 22.00, 'UNPAID', 'REGISTERED', NOW()),

  -- 明天（Day 1）— 心内赵强上午 (1014): total=28, remain=28 → 0 人挂号（测试空白排班）
  -- 此处插入1条用于演示跨日预约
  (2012, 1007, NULL, 3, 7, 1014, 1, 'MANUAL',    'R20260012', 1, 22.00, 'PAID',   'REGISTERED', NOW())
ON DUPLICATE KEY UPDATE
  patient_id        = VALUES(patient_id),
  consultation_id   = VALUES(consultation_id),
  dept_id           = VALUES(dept_id),
  doctor_id         = VALUES(doctor_id),
  schedule_id       = VALUES(schedule_id),
  operator_user_id  = VALUES(operator_user_id),
  source            = VALUES(source),
  queue_no          = VALUES(queue_no),
  registration_fee  = VALUES(registration_fee),
  fee_status        = VALUES(fee_status),
  status            = VALUES(status),
  registered_at     = VALUES(registered_at);

-- ------------------------------------------------------------
-- 9. 分诊记录（与部分挂号对应）
-- ------------------------------------------------------------
INSERT INTO triage_record (
  triage_id, patient_id, consultation_id, registration_id, triage_doctor_id,
  recommended_dept_id, chief_complaint, risk_level, triage_result, status, created_at
)
VALUES
  (3001, 1001, 1001, 2001, NULL, 1, '头痛三天',         'LOW',    '建议神经内科普通门诊',   'COMPLETED',   NOW()),
  (3002, 1002, 1002, 2009, NULL, 3, '胸闷一周',         'MEDIUM', '建议心血管内科优先处理', 'AI_SUGGESTED', NOW()),
  (3003, 1005, 1003, 2011, NULL, 3, '持续高血压控制差', 'HIGH',   '建议心血管内科调整用药', 'COMPLETED',   NOW())
ON DUPLICATE KEY UPDATE
  patient_id          = VALUES(patient_id),
  consultation_id     = VALUES(consultation_id),
  registration_id     = VALUES(registration_id),
  triage_doctor_id    = VALUES(triage_doctor_id),
  recommended_dept_id = VALUES(recommended_dept_id),
  chief_complaint     = VALUES(chief_complaint),
  risk_level          = VALUES(risk_level),
  triage_result       = VALUES(triage_result),
  status              = VALUES(status),
  created_at          = VALUES(created_at);

-- ------------------------------------------------------------
-- 10. AI 排班建议（2条建议 + 详情，测试 AI 建议功能）
-- ------------------------------------------------------------
DELETE FROM ai_schedule_suggestion_detail WHERE suggestion_id IN (2001, 2002);
DELETE FROM ai_schedule_suggestion WHERE suggestion_id IN (2001, 2002);

INSERT INTO ai_schedule_suggestion (
  suggestion_id, doctor_id, dept_id, work_date, time_period,
  suggested_quota, suggestion_reason, status, created_at
)
VALUES
  (2001, 1, 1, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'MORNING',   30, '神经内科上午就诊需求较高，建议增加主任医师上午号源。',    'PENDING', NOW()),
  (2002, 7, 3, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'AFTERNOON', 28, '根据历史就诊数据，心血管内科周四下午时段常出现排队高峰，建议增加主任医师排班。', 'PENDING', NOW());

INSERT INTO ai_schedule_suggestion_detail (
  suggestion_id, doctor_id, doctor_name, schedule_date, time_slot, max_appointments, reason, status
)
VALUES
  (2001, 1, '张明', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'MORNING',   30, '近期神经内科上午挂号需求较高，张明医生专长匹配头痛和脑血管病。',       'PENDING'),
  (2001, 2, '李华', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'AFTERNOON', 25, '下午时段可覆盖复诊患者，避免上午队列过长。',                             'PENDING'),
  (2002, 7, '赵强', DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'AFTERNOON', 28, '心血管内科周四下午复诊量持续偏高，建议增加赵强主任医师号源以减少等待时间。', 'PENDING');