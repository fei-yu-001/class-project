# 工资管理系统优化设计规划

## 1. 项目背景与目标

### 1.1 当前状态

本系统为数据库课程设计项目，采用前后端分离架构：

- **后端**：Spring Boot 3.2 + JPA + MySQL + Redis
- **前端**：Vue 3 + TypeScript + Vite + Tailwind CSS
- **安全**：JWT + Spring Security + XSS 过滤 + Redis 限流

当前已实现基础数据维护、考勤/请假/加班/绩效记录、工资预览/生成/审核/发放、仪表盘等业务闭环，但部分细节与真实企业场景存在差距。

### 1.2 优化目标

将系统从"课程演示版"升级为"贴近真实企业业务"的工资管理系统，重点解决：

1. 现有前端字段不一致、搜索失效等明显 bug
2. 业务字段缺失（审批时间、审批人、支付流水号等）
3. 数据权限不足（普通员工与管理员看到的数据无区分）
4. 审批流程不完整（缺少提交-审批-驳回闭环）
5. 报表导出、操作日志等企业级功能缺失

## 2. 总体设计原则

- **渐进式迭代**：先修复 bug，再补字段，再做权限，最后做审批流和报表
- **最小改动**：优先在现有结构上扩展，避免大规模重构
- **真实业务导向**：所有新增字段和流程必须能在真实企业场景中找到对应
- **安全优先**：数据权限改造必须在后端完成，不能仅依赖前端隐藏

## 3. 分阶段实施计划

### 阶段一：修复现有功能（让系统先跑顺）

**目标**：修复前端字段名不一致、搜索参数不匹配等问题，让现有功能真正可用。

#### 后端调整

| 文件 | 调整内容 |
|------|---------|
| `EmployeeController.java` | 将 `employmentStatus` 参数重命名为 `empStatus`，与前端对齐 |

#### 前端调整

| 文件 | 调整内容 |
|------|---------|
| `EmployeeView.vue` | 确认员工下拉/显示字段使用 `empId` / `empName` |
| `SalaryView.vue` | 新增/编辑工资弹窗：员工下拉改为 `e.empId` / `e.empName`；搜索员工姓名改为 `empName` |
| `LeaveView.vue` | 员工下拉/表格显示改为 `empName` |
| `OvertimeView.vue` | 员工下拉/表格显示改为 `empName` |
| `AttendanceView.vue` | 员工下拉/表格显示改为 `empName` |
| `PerformanceView.vue` | 员工下拉/表格显示改为 `empName` |
| 各视图 | 将剩余 `alert()` 统一替换为 `ToastMessage` |

#### 交互优化

- 考勤状态选择"缺勤"时，自动勾选"记为缺勤"复选框
- 考勤状态选择"正常"时，自动清空迟到/早退分钟并取消缺勤

---

### 阶段二：补齐真实业务字段

**目标**：让数据模型和页面看起来像真实企业系统，补充时间、审批人、备注、流水号等关键信息。

#### 2.1 请假模块

**数据库变更**：

```sql
ALTER TABLE leave_req ADD COLUMN applied_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间';
ALTER TABLE leave_req ADD COLUMN approved_at DATETIME DEFAULT NULL COMMENT '审批时间';
ALTER TABLE leave_req ADD COLUMN approved_by INT DEFAULT NULL COMMENT '审批人用户ID';
ALTER TABLE leave_req ADD COLUMN remark VARCHAR(255) DEFAULT NULL COMMENT '备注';
```

**前端调整**：

- 列表显示申请时间、审批时间、审批人、备注
- 弹窗增加备注输入框
- 新增时自动填充 `applied_at`

#### 2.2 加班模块

**数据库变更**：

```sql
ALTER TABLE overtime ADD COLUMN applied_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间';
ALTER TABLE overtime ADD COLUMN approved_at DATETIME DEFAULT NULL COMMENT '审批时间';
ALTER TABLE overtime ADD COLUMN approved_by INT DEFAULT NULL COMMENT '审批人用户ID';
ALTER TABLE overtime ADD COLUMN remark VARCHAR(255) DEFAULT NULL COMMENT '备注';
ALTER TABLE overtime ADD COLUMN ot_type VARCHAR(20) DEFAULT 'WORKDAY' COMMENT '加班类型：WORKDAY/Weekend/HOLIDAY';
```

**业务规则**：

- 工作日加班：1.5 倍工资
- 周末加班：2 倍工资
- 节假日加班：3 倍工资

**工资计算调整**：

```java
BigDecimal rate = switch(otType) {
    case "HOLIDAY" -> new BigDecimal("3.0");
    case "WEEKEND" -> new BigDecimal("2.0");
    default -> new BigDecimal("1.5");
};
BigDecimal overtimePay = hourlySalary.multiply(approvedOvertimeHours).multiply(rate);
```

#### 2.3 考勤模块

**数据库变更**：

```sql
ALTER TABLE attendance ADD COLUMN check_in TIME DEFAULT NULL COMMENT '上班打卡时间';
ALTER TABLE attendance ADD COLUMN check_out TIME DEFAULT NULL COMMENT '下班打卡时间';
ALTER TABLE attendance ADD COLUMN work_shift VARCHAR(20) DEFAULT 'NORMAL' COMMENT '班次';
```

**业务规则**：

- 标准班次：09:00-18:00
- 09:00 之后打卡计为迟到
- 18:00 之前打卡计为早退
- 前端允许手动填写打卡时间，或选择考勤状态自动生成迟到/早退分钟

#### 2.4 工资模块

**数据库变更**：

```sql
ALTER TABLE salary_record ADD COLUMN paid_by INT DEFAULT NULL COMMENT '发放人用户ID';
ALTER TABLE salary_record ADD COLUMN payment_ref VARCHAR(100) DEFAULT NULL COMMENT '支付流水号/银行单号';
ALTER TABLE salary_record ADD COLUMN payment_method VARCHAR(20) DEFAULT NULL COMMENT '发放方式';
```

**业务规则**：

- 工资审核后状态为 `APPROVED`
- 工资发放时必须填写 `payment_ref`（如银行转账流水号）
- 发放按钮点击后弹出确认框要求输入流水号
- 工资明细关联 `bonus` / `deduction` 表，支持手动添加奖金/扣款项

#### 2.5 职位变动模块

**数据库变更**：

```sql
ALTER TABLE pos_change ADD COLUMN change_date DATE NOT NULL DEFAULT (CURDATE()) COMMENT '变动日期';
ALTER TABLE pos_change ADD COLUMN old_pos_id VARCHAR(10) DEFAULT NULL COMMENT '原职位';
ALTER TABLE pos_change ADD COLUMN new_pos_id VARCHAR(10) NOT NULL COMMENT '新职位';
ALTER TABLE pos_change ADD COLUMN old_dept_code VARCHAR(10) DEFAULT NULL COMMENT '原部门';
ALTER TABLE pos_change ADD COLUMN new_dept_code VARCHAR(10) NOT NULL COMMENT '新部门';
ALTER TABLE pos_change ADD COLUMN change_type VARCHAR(20) DEFAULT 'PROMOTION' COMMENT '变动类型：PROMOTION/DEMOTION/TRANSFER';
```

**业务规则**：

- 新增职位变动时，同步更新 `employee` 表的 `pos_id` 和 `dept_code`
- 记录原职位/部门，便于追溯
- 变动类型：晋升、降职、调岗

---

### 阶段三：数据权限 + 普通员工视角

**目标**：区分管理员和普通员工的数据范围，让系统具备企业内系统的基本权限控制。

#### 权限模型

| 角色 | 权限范围 |
|------|---------|
| SUPER_ADMIN | 所有权限，包括用户管理 |
| ADMIN | 管理所有业务数据，但不能修改超级管理员 |
| USER | 只能查看和操作与自己员工号关联的数据 |

#### 后端改造

- 在 `User` 和 `Employee` 之间建立关联（`users.emp_id`）
- 所有查询接口根据当前登录用户的 `emp_id` 过滤数据
- 普通用户访问 `/api/salaries/search` 时，强制加上 `empId = 当前用户 emp_id`
- 普通用户访问 `/api/attendance/records` 时，只返回自己的考勤
- 新增 `@CurrentUser` 注解或工具方法获取当前登录用户

#### 前端改造

- 新增"我的工资"页面：显示当前登录员工的工资条
- 新增"我的考勤"、"我的请假"、"我的加班"入口（或在现有页面中根据角色自动过滤）
- 侧边栏菜单按角色动态显示
- 普通员工登录后默认跳转到"我的工资"或"个人中心"

---

### 阶段四：审批流 + 报表导出

**目标**：形成完整的业务闭环，支持审批、导出和审计。

#### 审批流

**请假/加班流程**：

1. 员工提交 → 状态 `PENDING`
2. 管理员审批通过 → 状态 `APPROVED`，记录 `approved_at` 和 `approved_by`
3. 管理员审批驳回 → 状态 `REJECTED`，记录驳回原因

**工资流程**：

1. 生成 → `GENERATED`
2. 审核 → `APPROVED`，记录审核人
3. 发放 → `PAID`，记录发放人和支付流水号

#### 报表导出

- 工资条导出：PDF / Excel
- 部门工资汇总表
- 员工考勤汇总表
- 请假/加班统计表

#### 操作日志

- 记录关键操作：工资审核、工资发放、用户角色变更、员工删除等
- 日志表：`operation_log(id, user_id, action, target_type, target_id, detail, created_at)`

## 4. 数据库变更总览

| 表名 | 变更 |
|------|------|
| `leave_req` | 新增 `applied_at`, `approved_at`, `approved_by`, `remark` |
| `overtime` | 新增 `applied_at`, `approved_at`, `approved_by`, `remark`, `ot_type` |
| `attendance` | 新增 `check_in`, `check_out`, `work_shift` |
| `salary_record` | 新增 `paid_by`, `payment_ref`, `payment_method` |
| `pos_change` | 新增 `change_date`, `old_pos_id`, `new_pos_id`, `old_dept_code`, `new_dept_code`, `change_type` |
| `operation_log` | 新建 |

## 5. 接口调整

### 新增接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/attendance/leaves/{id}/approve` | 审批通过请假 |
| POST | `/api/attendance/leaves/{id}/reject` | 驳回请假 |
| POST | `/api/attendance/overtime/{id}/approve` | 审批通过加班 |
| POST | `/api/attendance/overtime/{id}/reject` | 驳回加班 |
| GET | `/api/salaries/my` | 当前登录员工工资列表 |
| GET | `/api/attendance/my-records` | 当前登录员工考勤 |
| GET | `/api/attendance/my-leaves` | 当前登录员工请假 |
| GET | `/api/attendance/my-overtime` | 当前登录员工加班 |
| GET | `/api/salaries/{id}/export` | 导出工资条 |

### 调整接口

| 方法 | 路径 | 调整 |
|------|------|------|
| GET | `/api/employees/search` | 参数 `employmentStatus` → `empStatus` |
| POST | `/api/salaries/{id}/pay` | 请求体增加 `paymentRef` 和 `paymentMethod` |

## 6. 验收标准

### 阶段一

- [ ] 所有页面员工姓名正确显示
- [ ] 员工状态筛选生效
- [ ] 工资新增/编辑能正确选择员工
- [ ] 考勤状态与缺勤标记联动正确

### 阶段二

- [ ] 请假/加班记录包含申请时间、审批时间、审批人、备注
- [ ] 加班类型影响工资计算倍数
- [ ] 工资发放必须填写流水号
- [ ] 职位变动能正确更新员工当前职位/部门并记录原值

### 阶段三

- [ ] 普通员工只能看到自己的工资、考勤、请假、加班
- [ ] 管理员能看到全部数据
- [ ] 普通员工有"我的工资"独立入口

### 阶段四

- [ ] 请假/加班能完整走 PENDING → APPROVED/REJECTED 流程
- [ ] 工资发放记录操作人、审核人、流水号
- [ ] 能导出工资条 PDF/Excel
- [ ] 关键操作记录到操作日志

## 7. 风险与注意事项

1. **数据库变更**：所有表结构变更需要同步更新 `backend/sql/init.sql`
2. **数据兼容性**：现有 `salary_record` 中的旧数据需要为新增字段设置默认值
3. **权限安全**：数据权限过滤必须在后端完成，前端仅做界面隐藏
4. **工资计算**：加班倍数调整后，需要更新单元测试
5. **Redis 依赖**：限流和 Token 黑名单依赖 Redis，本地开发需启动 Redis

## 8. 下一步

确认本规划后，进入阶段一实施：修复现有 bug 和字段不一致问题。
