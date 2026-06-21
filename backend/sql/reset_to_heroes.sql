USE salary_management;

SET FOREIGN_KEY_CHECKS = 0;

-- 解除用户与员工的关联，避免删除员工时违反外键
UPDATE users SET emp_id = NULL WHERE emp_id IS NOT NULL;

-- 清空所有依赖员工表的业务数据
DELETE FROM sal_bonus_rel;
DELETE FROM sal_deduct_rel;
DELETE FROM bonus;
DELETE FROM deduction;
DELETE FROM salary_record;
DELETE FROM performance;
DELETE FROM attendance;
DELETE FROM leave_req;
DELETE FROM overtime;
DELETE FROM pos_change;
DELETE FROM project_member;
DELETE FROM payment_method;

-- 清空员工并重置自增 ID
DELETE FROM employee;
ALTER TABLE employee AUTO_INCREMENT = 1001;

-- 部门主管可能指向已删除员工，先置空
UPDATE department SET dept_manager_emp = NULL;

SET FOREIGN_KEY_CHECKS = 1;
