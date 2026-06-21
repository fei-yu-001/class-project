CREATE DATABASE IF NOT EXISTS salary_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE salary_management;

SET FOREIGN_KEY_CHECKS = 0;
DROP VIEW IF EXISTS v_emp_full, v_sal_detail, v_emp_perf_att, v_emp_project, v_emp_payinfo;
DROP TABLE IF EXISTS sal_deduct_rel, sal_bonus_rel, deduction, bonus, salary_record;
DROP TABLE IF EXISTS pos_change, overtime, leave_req, attendance, performance;
DROP TABLE IF EXISTS project_member, project, payment_method, users, employee, `position`, department;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. users
CREATE TABLE users(
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) DEFAULT NULL,
    emp_id INT DEFAULT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN DEFAULT TRUE,
    avatar VARCHAR(255) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(id),
    UNIQUE KEY uk_users_username(username)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='users';

-- 2. department
CREATE TABLE department(
       dept_code VARCHAR(10) NOT NULL COMMENT 'dept_code',
       dept_name VARCHAR(50) NOT NULL COMMENT 'dept_name',
       parent_dept_code VARCHAR(10) DEFAULT NULL COMMENT 'parent_dept_code',
       dept_manager_emp INT DEFAULT NULL COMMENT 'dept_manager_emp',
       PRIMARY KEY(dept_code)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='department';

-- 3. position
CREATE TABLE `position`(
     pos_id VARCHAR(10) NOT NULL COMMENT 'pos_id',
     pos_name VARCHAR(50) NOT NULL COMMENT 'pos_name',
     base_salary DECIMAL(10,2) NOT NULL COMMENT 'base_salary',
     dept_code VARCHAR(10) NOT NULL COMMENT 'dept_code',
     PRIMARY KEY(pos_id),
     FOREIGN KEY(dept_code) REFERENCES department(dept_code) ON DELETE RESTRICT ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='position';

-- 4. employee
CREATE TABLE employee(
     emp_id INT NOT NULL AUTO_INCREMENT COMMENT 'emp_id',
     emp_name VARCHAR(30) NOT NULL COMMENT 'emp_name',
     gender CHAR(1) NOT NULL COMMENT 'gender',
     birth_date DATE DEFAULT NULL COMMENT 'birth_date',
     hire_date DATE NOT NULL COMMENT 'hire_date',
     emp_status VARCHAR(12) NOT NULL COMMENT 'emp_status',
     pos_id VARCHAR(10) NOT NULL COMMENT 'pos_id',
     dept_code VARCHAR(10) NOT NULL COMMENT 'dept_code',
     PRIMARY KEY(emp_id),
     FOREIGN KEY(pos_id) REFERENCES `position`(pos_id) ON DELETE RESTRICT ON UPDATE CASCADE,
     FOREIGN KEY(dept_code) REFERENCES department(dept_code) ON DELETE RESTRICT ON UPDATE CASCADE
)ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COMMENT='employee';

ALTER TABLE department ADD CONSTRAINT fk_dept_mgr FOREIGN KEY(dept_manager_emp) REFERENCES employee(emp_id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE users ADD CONSTRAINT fk_users_emp FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE SET NULL ON UPDATE CASCADE;

-- 5. payment_method
CREATE TABLE payment_method(
           emp_id INT NOT NULL COMMENT 'emp_id',
           pay_type VARCHAR(20) NOT NULL COMMENT 'pay_type',
           account_no VARCHAR(100) NOT NULL COMMENT 'account_no',
           card_type VARCHAR(20) DEFAULT NULL COMMENT 'card_type',
           PRIMARY KEY(emp_id),
           FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='payment_method';

-- 6. project
CREATE TABLE project(
    proj_id INT NOT NULL AUTO_INCREMENT COMMENT 'proj_id',
    proj_name VARCHAR(100) NOT NULL COMMENT 'proj_name',
    proj_status VARCHAR(20) NOT NULL COMMENT 'proj_status',
    PRIMARY KEY(proj_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='project';

-- 7. project_member (M:N)
CREATE TABLE project_member(
           emp_id INT NOT NULL COMMENT 'emp_id',
           proj_id INT NOT NULL COMMENT 'proj_id',
           role_name VARCHAR(30) DEFAULT NULL COMMENT 'role_name',
           contrib_coeff DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT 'contrib_coeff',
           PRIMARY KEY(emp_id,proj_id),
           FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE,
           FOREIGN KEY(proj_id) REFERENCES project(proj_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='project_member';

-- 8. performance
CREATE TABLE performance(
        review_id INT NOT NULL AUTO_INCREMENT COMMENT 'review_id',
        emp_id INT NOT NULL COMMENT 'emp_id',
        grade VARCHAR(10) NOT NULL COMMENT 'grade',
        review_period VARCHAR(7) NOT NULL COMMENT 'review_period',
        PRIMARY KEY(review_id),
        FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='performance';

-- 9. attendance
CREATE TABLE attendance(
       att_id INT NOT NULL AUTO_INCREMENT COMMENT 'att_id',
       emp_id INT NOT NULL COMMENT 'emp_id',
       att_date DATE NOT NULL COMMENT 'att_date',
       att_status VARCHAR(20) NOT NULL COMMENT 'att_status',
       late_minutes INT NOT NULL DEFAULT 0 COMMENT 'late_minutes',
       early_leave_minutes INT NOT NULL DEFAULT 0 COMMENT 'early_leave_minutes',
       absent BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'absent',
       PRIMARY KEY(att_id),
       FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='attendance';

-- 10. leave_req
CREATE TABLE leave_req(
      leave_id INT NOT NULL AUTO_INCREMENT COMMENT 'leave_id',
      emp_id INT NOT NULL COMMENT 'emp_id',
      leave_type VARCHAR(20) NOT NULL COMMENT 'leave_type',
      start_date DATE NOT NULL COMMENT 'start_date',
      end_date DATE NOT NULL COMMENT 'end_date',
      leave_days DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT 'leave_days',
      approval_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED' COMMENT 'approval_status',
      PRIMARY KEY(leave_id),
      FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='leave_req';

-- 11. overtime
CREATE TABLE overtime(
     ot_id INT NOT NULL AUTO_INCREMENT COMMENT 'ot_id',
     emp_id INT NOT NULL COMMENT 'emp_id',
     ot_hours DECIMAL(4,1) NOT NULL COMMENT 'ot_hours',
     ot_date DATE NOT NULL COMMENT 'ot_date',
     approval_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED' COMMENT 'approval_status',
     PRIMARY KEY(ot_id),
     FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='overtime';

-- 12. pos_change
CREATE TABLE pos_change(
       change_id INT NOT NULL AUTO_INCREMENT COMMENT 'change_id',
       emp_id INT NOT NULL COMMENT 'emp_id',
       change_reason VARCHAR(100) DEFAULT NULL COMMENT 'change_reason',
       PRIMARY KEY(change_id),
       FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='pos_change';

-- 13. salary_record
CREATE TABLE salary_record(
          salary_id INT NOT NULL AUTO_INCREMENT COMMENT 'salary_id',
          emp_id INT NOT NULL COMMENT 'emp_id',
          base_snap DECIMAL(10,2) NOT NULL COMMENT 'base_snap',
          pay_period VARCHAR(7) NOT NULL COMMENT 'pay_period',
          gross_total DECIMAL(10,2) NOT NULL COMMENT 'gross_total',
          deduct_total DECIMAL(10,2) NOT NULL COMMENT 'deduct_total',
          net_pay DECIMAL(10,2) NOT NULL COMMENT 'net_pay',
          performance_bonus DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'performance_bonus',
          full_attendance_bonus DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'full_attendance_bonus',
          overtime_pay DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'overtime_pay',
          extra_bonus DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'extra_bonus',
          leave_deduction DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'leave_deduction',
          attendance_deduction DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'attendance_deduction',
          extra_deduction DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'extra_deduction',
          tax_deduction DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'tax_deduction',
          insurance_deduction DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'insurance_deduction',
          status VARCHAR(20) NOT NULL DEFAULT 'GENERATED' COMMENT 'status',
          generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'generated_at',
          approved_at DATETIME DEFAULT NULL COMMENT 'approved_at',
          paid_at DATETIME DEFAULT NULL COMMENT 'paid_at',
          PRIMARY KEY(salary_id),
          UNIQUE KEY uk_salary_emp_period(emp_id, pay_period),
          FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='salary_record';

-- 14. bonus
CREATE TABLE bonus(
  bonus_id INT NOT NULL AUTO_INCREMENT COMMENT 'bonus_id',
  emp_id INT NOT NULL COMMENT 'emp_id',
  pay_period VARCHAR(7) NOT NULL COMMENT 'pay_period',
  bonus_type VARCHAR(20) NOT NULL COMMENT 'bonus_type',
  pre_tax_amt DECIMAL(10,2) NOT NULL COMMENT 'pre_tax_amt',
  remark VARCHAR(255) DEFAULT NULL COMMENT 'remark',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'created_at',
  PRIMARY KEY(bonus_id),
  FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='bonus';

-- sal_bonus_rel (M:N)
CREATE TABLE sal_bonus_rel(
          salary_id INT NOT NULL,
          bonus_id INT NOT NULL,
          PRIMARY KEY(salary_id,bonus_id),
          FOREIGN KEY(salary_id) REFERENCES salary_record(salary_id) ON DELETE CASCADE,
          FOREIGN KEY(bonus_id) REFERENCES bonus(bonus_id) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='sal_bonus_rel';

-- 15. deduction
CREATE TABLE deduction(
      deduct_id INT NOT NULL AUTO_INCREMENT COMMENT 'deduct_id',
      emp_id INT NOT NULL COMMENT 'emp_id',
      pay_period VARCHAR(7) NOT NULL COMMENT 'pay_period',
      deduct_type VARCHAR(20) NOT NULL COMMENT 'deduct_type',
      deduct_amt DECIMAL(10,2) NOT NULL COMMENT 'deduct_amt',
      remark VARCHAR(255) DEFAULT NULL COMMENT 'remark',
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'created_at',
      PRIMARY KEY(deduct_id),
      FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='deduction';

-- sal_deduct_rel (M:N)
CREATE TABLE sal_deduct_rel(
           salary_id INT NOT NULL,
           deduct_id INT NOT NULL,
           PRIMARY KEY(salary_id,deduct_id),
           FOREIGN KEY(salary_id) REFERENCES salary_record(salary_id) ON DELETE CASCADE,
           FOREIGN KEY(deduct_id) REFERENCES deduction(deduct_id) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='sal_deduct_rel';

-- ==============================================
-- Indexes
-- ==============================================
CREATE INDEX idx_dept_name ON department(dept_name);
CREATE INDEX idx_pos_name ON `position`(pos_name);
CREATE INDEX idx_emp_name ON employee(emp_name);
CREATE INDEX idx_emp_status ON employee(emp_status);
CREATE INDEX idx_sal_period ON salary_record(pay_period);
CREATE INDEX idx_bonus_type ON bonus(bonus_type);
CREATE INDEX idx_bonus_emp_period ON bonus(emp_id, pay_period);
CREATE INDEX idx_deduct_type ON deduction(deduct_type);
CREATE INDEX idx_deduct_emp_period ON deduction(emp_id, pay_period);
CREATE INDEX idx_perf_emp ON performance(emp_id);
CREATE INDEX idx_perf_period ON performance(review_period);
CREATE INDEX idx_att_emp ON attendance(emp_id);
CREATE INDEX idx_att_date ON attendance(att_date);
CREATE INDEX idx_leave_emp ON leave_req(emp_id);
CREATE INDEX idx_leave_dates ON leave_req(start_date, end_date);
CREATE INDEX idx_ot_emp ON overtime(emp_id);
CREATE INDEX idx_ot_date ON overtime(ot_date);
CREATE INDEX idx_change_emp ON pos_change(emp_id);
CREATE INDEX idx_pay_emp ON payment_method(emp_id);
CREATE INDEX idx_proj_name ON project(proj_name);

-- ==============================================
-- Views
-- ==============================================
CREATE VIEW v_emp_full AS
SELECT e.emp_id,e.emp_name,e.gender,e.birth_date,e.hire_date,e.emp_status,
p.pos_id,p.pos_name,p.base_salary,
d.dept_code,d.dept_name
FROM employee e
JOIN `position` p ON e.pos_id=p.pos_id
JOIN department d ON e.dept_code=d.dept_code;

CREATE VIEW v_sal_detail AS
SELECT s.salary_id,s.pay_period,s.gross_total,s.deduct_total,s.net_pay,
e.emp_id,e.emp_name,
b.bonus_id,b.bonus_type,b.pre_tax_amt,
d.deduct_id,d.deduct_type,d.deduct_amt
FROM salary_record s
JOIN employee e ON s.emp_id=e.emp_id
LEFT JOIN sal_bonus_rel sb ON s.salary_id=sb.salary_id
LEFT JOIN bonus b ON sb.bonus_id=b.bonus_id
LEFT JOIN sal_deduct_rel sd ON s.salary_id=sd.salary_id
LEFT JOIN deduction d ON sd.deduct_id=d.deduct_id;

CREATE VIEW v_emp_perf_att AS
SELECT e.emp_id,e.emp_name,pr.grade,pr.review_period,a.att_date,a.att_status
FROM employee e
LEFT JOIN performance pr ON e.emp_id=pr.emp_id
LEFT JOIN attendance a ON e.emp_id=a.emp_id;

CREATE VIEW v_emp_project AS
SELECT e.emp_id,e.emp_name,p.proj_id,p.proj_name,pm.role_name,pm.contrib_coeff
FROM employee e
JOIN project_member pm ON e.emp_id=pm.emp_id
JOIN project p ON pm.proj_id=p.proj_id;

CREATE VIEW v_emp_payinfo AS
SELECT e.emp_id,e.emp_name,pm.pay_type,pm.account_no,pm.card_type
FROM employee e
JOIN payment_method pm ON e.emp_id=pm.emp_id;

-- ==============================================
-- Sample Data
-- ==============================================
INSERT INTO department (dept_code, dept_name, parent_dept_code) VALUES
('D001', '技术部', NULL),
('D002', '市场部', NULL),
('D003', '财务部', NULL),
('D004', '人事部', NULL);

INSERT INTO `position` (pos_id, pos_name, base_salary, dept_code) VALUES
('P001', '技术经理', 8000.00, 'D001'),
('P002', '技术组长', 6000.00, 'D001'),
('P003', '软件工程师', 5000.00, 'D001'),
('P004', '市场经理', 8000.00, 'D002'),
('P005', '市场专员', 5000.00, 'D002'),
('P006', '财务经理', 8000.00, 'D003'),
('P007', '会计', 5000.00, 'D003'),
('P008', '人事经理', 8000.00, 'D004'),
('P009', '人事专员', 5000.00, 'D004');

INSERT INTO employee (emp_name, gender, birth_date, hire_date, emp_status, pos_id, dept_code) VALUES
('张三', '男', '1990-03-15', '2023-01-15', '在职', 'P001', 'D001'),
('李四', '女', '1992-07-20', '2023-03-20', '在职', 'P002', 'D001'),
('王五', '男', '1995-11-10', '2023-06-10', '在职', 'P003', 'D001'),
('赵六', '女', '1988-05-01', '2023-02-01', '在职', 'P004', 'D002'),
('孙七', '男', '1993-09-18', '2023-07-15', '在职', 'P005', 'D002');

UPDATE department SET dept_manager_emp = 1001 WHERE dept_code = 'D001';
UPDATE department SET dept_manager_emp = 1004 WHERE dept_code = 'D002';

INSERT INTO users (username, password, nickname, emp_id, role, enabled) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', NULL, 'ADMIN', TRUE),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', 1001, 'USER', TRUE);

INSERT INTO payment_method (emp_id, pay_type, account_no, card_type) VALUES
(1001, 'BANK_CARD', '6222000000001', '工商银行'),
(1002, 'ALIPAY', 'lisi@alipay.com', NULL),
(1003, 'BANK_CARD', '6222000000003', '建设银行'),
(1004, 'ALIPAY', 'zhaoliu@alipay.com', NULL),
(1005, 'BANK_CARD', '6222000000005', '招商银行');

INSERT INTO project (proj_name, proj_status) VALUES
('企业工资管理系统', '进行中'),
('客户管理系统', '已完成'),
('数据分析平台', '进行中');

INSERT INTO project_member (emp_id, proj_id, role_name, contrib_coeff) VALUES
(1001, 1, '项目负责人', 0.50),
(1002, 1, '技术组长', 0.30),
(1003, 1, '开发工程师', 0.20),
(1003, 3, '开发工程师', 1.00),
(1004, 2, '市场负责人', 1.00);

INSERT INTO performance (emp_id, grade, review_period) VALUES
(1001, 'A', '2026-06'),
(1002, 'B', '2026-06'),
(1003, 'C', '2026-06'),
(1004, 'A', '2026-06'),
(1005, 'D', '2026-06');

INSERT INTO attendance (emp_id, att_date, att_status, late_minutes, early_leave_minutes, absent) VALUES
(1001, '2026-06-01', '正常', 0, 0, FALSE),
(1002, '2026-06-02', '正常', 0, 0, FALSE),
(1003, '2026-06-03', '迟到', 20, 0, FALSE),
(1004, '2026-06-04', '早退', 0, 30, FALSE),
(1005, '2026-06-05', '缺勤', 0, 0, TRUE);

INSERT INTO leave_req (emp_id, leave_type, start_date, end_date, leave_days, approval_status) VALUES
(1003, '病假', '2026-06-10', '2026-06-10', 1.00, 'APPROVED'),
(1005, '事假', '2026-06-12', '2026-06-13', 2.00, 'APPROVED');

INSERT INTO overtime (emp_id, ot_hours, ot_date, approval_status) VALUES
(1001, 3.0, '2026-06-08', 'APPROVED'),
(1002, 2.5, '2026-06-09', 'APPROVED'),
(1005, 4.0, '2026-06-11', 'PENDING');

INSERT INTO salary_record (
  emp_id, base_snap, pay_period, gross_total, deduct_total, net_pay,
  performance_bonus, full_attendance_bonus, overtime_pay, leave_deduction,
  attendance_deduction, tax_deduction, insurance_deduction, status, generated_at, approved_at, paid_at
) VALUES
(1001, 8000.00, '2026-05', 9100.00, 0.00, 9100.00, 800.00, 300.00, 0.00, 0.00, 0.00, 0.00, 0.00, 'PAID', '2026-05-31 10:00:00', '2026-05-31 11:00:00', '2026-05-31 12:00:00'),
(1002, 6000.00, '2026-05', 6600.00, 0.00, 6600.00, 300.00, 300.00, 0.00, 0.00, 0.00, 0.00, 0.00, 'APPROVED', '2026-05-31 10:00:00', '2026-05-31 11:00:00', NULL),
(1003, 5000.00, '2026-05', 5000.00, 50.00, 4950.00, 0.00, 0.00, 0.00, 0.00, 50.00, 0.00, 0.00, 'GENERATED', '2026-05-31 10:00:00', NULL, NULL);

INSERT INTO bonus (emp_id, pay_period, bonus_type, pre_tax_amt, remark) VALUES
(1001, '2026-05', '项目奖', 1000.00, '季度项目分红'),
(1002, '2026-05', '绩效奖', 500.00, '月度绩效奖励');

INSERT INTO deduction (emp_id, pay_period, deduct_type, deduct_amt, remark) VALUES
(1001, '2026-05', '个人所得税', 500.00, '代扣个税'),
(1001, '2026-05', '社保', 300.00, '代扣社保');

INSERT INTO sal_bonus_rel (salary_id, bonus_id) VALUES
(1, 1),
(2, 2);

INSERT INTO sal_deduct_rel (salary_id, deduct_id) VALUES
(1, 1),
(1, 2),
(2, 2);
