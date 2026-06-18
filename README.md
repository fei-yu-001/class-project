# 工资管理系统

面向数据库课程设计的前后端分离工资管理系统。系统围绕“员工基础数据 → 考勤/请假/加班/绩效 → 工资预览 → 工资生成 → 审核 → 发放”的业务闭环设计，保留原始 `salary_management(1)(1).sql` 作为课程基准文件，实际运行脚本使用 `backend/sql/init.sql`。

## 架构

前后端分离架构：

| 模式 | 前端 | 后端 | 说明 |
|------|------|------|------|
| 开发 | Vite Dev Server (`:3000`) | Spring Boot (`:8088`) | 前后端独立运行，Vite 代理 `/api` 到后端 |
| 生产 | 构建到 `resources/static` | Spring Boot (`:8088`) | 后端托管前端静态资源，单端口部署 |

## 目录结构

- `frontend/`：Vue 3 + TypeScript 前端源码
- `backend/`：Spring Boot 后端源码、SQL 脚本
- `backend/sql/`：数据库初始化脚本
- `backend/cloudflare/`：Cloudflare Tunnel 配置（公网访问）
- `salary_management(1)(1).sql`：小组给定的 SQL 基准版本（ER 图设计），保持不改动

## 核心功能

- 员工、部门、职位、支付方式、项目等基础数据维护
- 考勤记录：支持考勤日期、迟到分钟、早退分钟、缺勤标记
- 请假记录：支持开始日期、结束日期、请假天数、审批状态
- 加班记录：支持加班日期、加班时长、审批状态
- 绩效考核：支持按 `yyyy-MM` 考核周期录入 A/B/C/D 等级
- 工资核算：支持预览、生成、审核、发放、明细展示
- 仪表盘：展示员工、组织、支付方式、工资趋势、待审核和已发放统计

## 工资计算规则

课程设计版采用固定、可解释的工资规则：

- 绩效奖：A/B/C/D 分别为基本工资的 `10% / 5% / 0% / -3%`
- 加班工资：`基本工资 / 21.75 / 8 * 加班小时 * 1.5`
- 全勤奖：`300`，存在缺勤、请假、迟到或早退时不发
- 请假扣款：按日薪扣除，`基本工资 / 21.75 * 请假天数`
- 考勤扣款：迟到、早退每次扣 `50`；缺勤按日薪扣除
- 实发工资：`基本工资 + 绩效奖 + 全勤奖 + 加班工资 - 请假扣款 - 考勤扣款`

第一版不做真实个税和社保分档，相关字段保留为扩展项。

## 工资 API

- `GET /api/salaries/preview?payPeriod=yyyy-MM`：预览本期工资明细
- `POST /api/salaries/generate`：生成本期工资，状态为 `GENERATED`
- `POST /api/salaries/{id}/approve`：审核工资，状态变为 `APPROVED`
- `POST /api/salaries/{id}/pay`：发放工资，状态变为 `PAID`
- `PUT /api/salaries/{id}` / `DELETE /api/salaries/{id}`：已发放工资禁止编辑和删除

## 本地开发启动

### 1. 启动数据库

使用 Navicat 或命令行执行初始化脚本。注意：脚本会重建 `salary_management` 数据库，执行前请备份本地重要数据。

```
backend/sql/init.sql
```

### 2. 启动 Redis

确保 Redis 在 `localhost:6379` 运行。

### 3. 启动后端

在 IDEA 中打开 `backend/` 目录，运行 `SalaryManagementApplication.java`。

- 端口：`8088`
- Profile：`dev`
- 数据库：`localhost:3306/salary_management`

### 4. 启动前端

在 VSCode 中打开 `frontend/` 目录，执行：

```bash
npm install
npm run dev
```

- 开发服务器：`http://localhost:3000`
- API 代理：`/api` → `http://localhost:8088`
- 前端修改实时热更新，无需重启后端

## 公网访问

通过 Cloudflare Tunnel 暴露到公网：

```
https://salary.feiyu.rest
```

启动方式：

```bash
backend\cloudflare\start-tunnel.bat
```

## 数据库说明

- `backend/sql/init.sql`：可运行的初始化脚本（包含表结构、视图、索引、示例数据）
- `salary_management(1)(1).sql`：小组给定的 SQL 基准版本（ER 图设计），保持不改动

## 验证命令

后端：

```bash
cd backend
mvn test
```

前端：

```bash
cd frontend
npm run build
```

## 超级管理员

| 用户名 | 密码 |
|--------|------|
| DDyu | zzf050731 |

首次启动后端时自动创建，可管理所有用户权限。

