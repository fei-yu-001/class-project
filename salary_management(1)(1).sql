CREATE DATABASE IF NOT EXISTS salary_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE salary_management;

--1.部门 department 主码：部门代码
CREATE TABLE department(
       dept_code VARCHAR(10) NOT NULL COMMENT '部门代码',
       dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
       parent_dept_code VARCHAR(10) DEFAULT NULL COMMENT '上级部门代码',
       dept_manager_emp INT DEFAULT NULL COMMENT '部门负责人工号',
       PRIMARY KEY(dept_code)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

--2.职位 position 主码：职位ID
CREATE TABLE position(
     pos_id VARCHAR(10) NOT NULL COMMENT '职位ID',
     pos_name VARCHAR(50) NOT NULL COMMENT '职位名称',
     base_salary DECIMAL(10,2) NOT NULL COMMENT '基本工资',
     dept_code VARCHAR(10) NOT NULL COMMENT '所属部门代码',
     PRIMARY KEY(pos_id),
     FOREIGN KEY(dept_code) REFERENCES department(dept_code) ON DELETE RESTRICT ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位表';

--3.员工 employee 主码：工号
CREATE TABLE employee(
     emp_id INT NOT NULL AUTO_INCREMENT COMMENT '工号',
     emp_name VARCHAR(30) NOT NULL COMMENT '姓名',
     gender CHAR(1) NOT NULL COMMENT '性别',
     birth_date DATE DEFAULT NULL COMMENT '出生日期',
     hire_date DATE NOT NULL COMMENT '入职日期',
     emp_status VARCHAR(12) NOT NULL COMMENT '在职状态',
     pos_id VARCHAR(10) NOT NULL COMMENT '现任职位ID',
     dept_code VARCHAR(10) NOT NULL COMMENT '所属部门代码',
     PRIMARY KEY(emp_id),
     FOREIGN KEY(pos_id) REFERENCES position(pos_id) ON DELETE RESTRICT ON UPDATE CASCADE,
     FOREIGN KEY(dept_code) REFERENCES department(dept_code) ON DELETE RESTRICT ON UPDATE CASCADE
)ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

--补部门自关联外键（负责人引用员工）
ALTER TABLE department ADD CONSTRAINT fk_dept_mgr FOREIGN KEY(dept_manager_emp) REFERENCES employee(emp_id) ON DELETE SET NULL ON UPDATE CASCADE;

--4.支付方式 payment_method 1:1绑定员工，主码：支付无独立主键，emp_id为主键（ER绑定1:1）
CREATE TABLE payment_method(
           emp_id INT NOT NULL COMMENT '员工工号',
           pay_type VARCHAR(20) NOT NULL COMMENT '支付方式类型',
           account_no VARCHAR(100) NOT NULL COMMENT '账号',
           card_type VARCHAR(20) DEFAULT NULL COMMENT '卡类型',
           PRIMARY KEY(emp_id),
           FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付方式表';

--5.项目 project 主码：项目ID
CREATE TABLE project(
    proj_id INT NOT NULL AUTO_INCREMENT COMMENT '项目ID',
    proj_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    proj_status VARCHAR(20) NOT NULL COMMENT '项目状态',
    PRIMARY KEY(proj_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

--6.员工<->项目 M:N【参与】中间表 project_member（ER多对多，属性：角色、贡献系数）
CREATE TABLE project_member(
           emp_id INT NOT NULL COMMENT '员工工号',
           proj_id INT NOT NULL COMMENT '项目ID',
           role_name VARCHAR(30) DEFAULT NULL COMMENT '角色',
           contrib_coeff DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '贡献系数',
           PRIMARY KEY(emp_id,proj_id),
           FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE,
           FOREIGN KEY(proj_id) REFERENCES project(proj_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员中间表';

--7.绩效考核 performance 主码：考核ID
CREATE TABLE performance(
        review_id INT NOT NULL AUTO_INCREMENT COMMENT '考核ID',
        emp_id INT NOT NULL COMMENT '员工工号',
        grade VARCHAR(10) NOT NULL COMMENT '绩效等级',
        PRIMARY KEY(review_id),
        FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绩效考核表';

--8.考勤记录 attendance 主码：考勤ID
CREATE TABLE attendance(
       att_id INT NOT NULL AUTO_INCREMENT COMMENT '考勤ID',
       emp_id INT NOT NULL COMMENT '员工工号',
       att_status VARCHAR(20) NOT NULL COMMENT '考勤状态',
       PRIMARY KEY(att_id),
       FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录表';

--9.请假申请 leave_req 主码：请假ID
CREATE TABLE leave_req(
      leave_id INT NOT NULL AUTO_INCREMENT COMMENT '请假申请ID',
      emp_id INT NOT NULL COMMENT '员工工号',
      leave_type VARCHAR(20) NOT NULL COMMENT '请假类型',
      PRIMARY KEY(leave_id),
      FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

--10.加班记录 overtime 主码：加班ID
CREATE TABLE overtime(
     ot_id INT NOT NULL AUTO_INCREMENT COMMENT '加班ID',
     emp_id INT NOT NULL COMMENT '员工工号',
     ot_hours DECIMAL(4,1) NOT NULL COMMENT '加班时长',
     PRIMARY KEY(ot_id),
     FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加班记录表';

--11.职位变动记录 pos_change 主码：变动ID
CREATE TABLE pos_change(
       change_id INT NOT NULL AUTO_INCREMENT COMMENT '变动ID',
       emp_id INT NOT NULL COMMENT '员工工号',
       change_reason VARCHAR(100) DEFAULT NULL COMMENT '变动原因',
       PRIMARY KEY(change_id),
       FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位变动记录表';

--12.工资记录 salary_record 主码：工资记录ID
CREATE TABLE salary_record(
          salary_id INT NOT NULL AUTO_INCREMENT COMMENT '工资记录ID',
          emp_id INT NOT NULL COMMENT '员工工号',
          base_snap DECIMAL(10,2) NOT NULL COMMENT '基本工资(快照)',
          pay_period VARCHAR(7) NOT NULL COMMENT '计薪周期',
          gross_total DECIMAL(10,2) NOT NULL COMMENT '应发合计',
          deduct_total DECIMAL(10,2) NOT NULL COMMENT '扣款合计',
          net_pay DECIMAL(10,2) NOT NULL COMMENT '实发工资',
          PRIMARY KEY(salary_id),
          FOREIGN KEY(emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工资记录表';

--13.奖金 bonus 主码：奖金编号
CREATE TABLE bonus(
  bonus_id INT NOT NULL AUTO_INCREMENT COMMENT '奖金编号',
  bonus_type VARCHAR(20) NOT NULL COMMENT '奖金类型',
  pre_tax_amt DECIMAL(10,2) NOT NULL COMMENT '税前金额',
  PRIMARY KEY(bonus_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖金表';

--工资与奖金M:N【包含】中间表
CREATE TABLE sal_bonus_rel(
          salary_id INT NOT NULL,
          bonus_id INT NOT NULL,
          PRIMARY KEY(salary_id,bonus_id),
          FOREIGN KEY(salary_id) REFERENCES salary_record(salary_id) ON DELETE CASCADE,
          FOREIGN KEY(bonus_id) REFERENCES bonus(bonus_id) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工资-奖金关联(包含)';

--14.扣款 deduction 主码：扣款编号
CREATE TABLE deduction(
      deduct_id INT NOT NULL AUTO_INCREMENT COMMENT '扣款编号',
      deduct_type VARCHAR(20) NOT NULL COMMENT '扣款类型',
      deduct_amt DECIMAL(10,2) NOT NULL COMMENT '金额',
      PRIMARY KEY(deduct_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扣款表';

--工资与扣款M:N【扣除】中间表
CREATE TABLE sal_deduct_rel(
           salary_id INT NOT NULL,
           deduct_id INT NOT NULL,
           PRIMARY KEY(salary_id,deduct_id),
           FOREIGN KEY(salary_id) REFERENCES salary_record(salary_id) ON DELETE CASCADE,
           FOREIGN KEY(deduct_id) REFERENCES deduction(deduct_id) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工资-扣款关联(扣除)';

-- ==============================================
-- ================= 全 部 索 引 =================
-- ==============================================
CREATE INDEX idx_dept_name ON department(dept_name);
CREATE INDEX idx_pos_name ON position(pos_name);
CREATE INDEX idx_emp_name ON employee(emp_name);
CREATE INDEX idx_emp_status ON employee(emp_status);
CREATE INDEX idx_sal_period ON salary_record(pay_period);
CREATE INDEX idx_bonus_type ON bonus(bonus_type);
CREATE INDEX idx_deduct_type ON deduction(deduct_type);
CREATE INDEX idx_perf_emp ON performance(emp_id);
CREATE INDEX idx_att_emp ON attendance(emp_id);
CREATE INDEX idx_leave_emp ON leave_req(emp_id);
CREATE INDEX idx_ot_emp ON overtime(emp_id);
CREATE INDEX idx_change_emp ON pos_change(emp_id);
CREATE INDEX idx_pay_emp ON payment_method(emp_id);
CREATE INDEX idx_proj_name ON project(proj_name);

-- ==============================================
-- ================= 全 部 视 图 =================
-- ==============================================
--视图1：员工全信息(员工+部门+职位)
CREATE VIEW v_emp_full AS
SELECT e.emp_id,e.emp_name,e.gender,e.birth_date,e.hire_date,e.emp_status,
p.pos_id,p.pos_name,p.base_salary,
d.dept_code,d.dept_name
FROM employee e
JOIN position p ON e.pos_id=p.pos_id
JOIN department d ON e.dept_code=d.dept_code;

--视图2：工资明细（工资+员工+奖金+扣款）
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

--视图3：员工考勤绩效视图
CREATE VIEW v_emp_perf_att AS
SELECT e.emp_id,e.emp_name,pr.grade,a.att_status
FROM employee e
LEFT JOIN performance pr ON e.emp_id=pr.emp_id
LEFT JOIN attendance a ON e.emp_id=a.att_id;

--视图4：员工项目参与视图
CREATE VIEW v_emp_project AS
SELECT e.emp_id,e.emp_name,p.proj_id,p.proj_name,pm.role_name,pm.contrib_coeff
FROM employee e
JOIN project_member pm ON e.emp_id=pm.emp_id
JOIN project p ON pm.proj_id=p.proj_id;

--视图5：员工薪资结算账户视图
CREATE VIEW v_emp_payinfo AS
SELECT e.emp_id,e.emp_name,pm.pay_type,pm.account_no,pm.card_type
FROM employee e
JOIN payment_method pm ON e.emp_id=pm.emp_id;