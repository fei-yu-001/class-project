# 阶段一：修复现有功能 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复工资管理系统中前端字段名不一致、搜索参数不匹配、交互不完善等现有问题，让系统现有功能真正可用。

**Architecture:** 保持现有前后端架构不变，仅做最小化修复：后端统一参数命名，前端统一使用 `empId`/`empName`，并优化考勤状态联动和错误提示方式。

**Tech Stack:** Spring Boot 3.2, Vue 3 + TypeScript, Tailwind CSS

---

## File Structure

### 后端

| 文件 | 责任 |
|------|------|
| `backend/src/main/java/com/salary/controller/EmployeeController.java` | 员工查询接口，需要把 `employmentStatus` 改为 `empStatus` |

### 前端

| 文件 | 责任 |
|------|------|
| `frontend/src/views/SalaryView.vue` | 工资管理：员工下拉/姓名显示字段修复 |
| `frontend/src/views/LeaveView.vue` | 请假：员工下拉/姓名显示字段修复 |
| `frontend/src/views/OvertimeView.vue` | 加班：员工下拉/姓名显示字段修复 |
| `frontend/src/views/AttendanceView.vue` | 考勤：员工下拉/姓名显示字段修复，状态联动 |
| `frontend/src/views/PerformanceView.vue` | 绩效：员工下拉/姓名显示字段修复 |
| `frontend/src/views/EmployeeView.vue` | 确认字段使用正确（empId/empName） |
| `frontend/src/views/ProfileView.vue` | 检查是否有 `alert()`，统一替换 |
| `frontend/src/views/ProjectView.vue` | 检查是否有 `alert()`，统一替换 |
| `frontend/src/views/PaymentView.vue` | 检查是否有 `alert()`，统一替换 |

---

## Task 1: 修复后端员工状态搜索参数

**Files:**
- Modify: `backend/src/main/java/com/salary/controller/EmployeeController.java:50`

- [ ] **Step 1: 修改参数名**

将 `employmentStatus` 改为 `empStatus`，与前端 `EmployeeView.vue` 对齐。

```java
@GetMapping("/search")
public Result<Page<Employee>> search(
        @RequestParam(required = false) String empName,
        @RequestParam(required = false) String deptCode,
        @RequestParam(required = false) String empStatus,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    return Result.success(employeeService.search(empName, deptCode, empStatus,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "empId"))));
}
```

- [ ] **Step 2: 确认 EmployeeService 方法签名一致**

`EmployeeService.search(String empName, String deptCode, String empStatus, Pageable pageable)` 已经是 `empStatus`，无需修改。

- [ ] **Step 3: 验证后端编译**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

---

## Task 2: 修复 SalaryView.vue 员工字段

**Files:**
- Modify: `frontend/src/views/SalaryView.vue`

- [ ] **Step 1: 修复 getEmployeeName 函数**

当前使用 `e.empName || e.name`，保留 `empName` 优先即可，但需确保员工下拉绑定正确。

```typescript
const getEmployeeName = (empId: number) => {
  const emp = employees.value.find(e => e.empId === empId || e.id === empId)
  return emp?.empName || emp?.name || '-'
}
```

- [ ] **Step 2: 修复新增/编辑弹窗的员工下拉**

将 `e.id` 和 `e.name` 改为 `e.empId` 和 `e.empName`。

```vue
<select v-model="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm">
  <option value="">请选择</option>
  <option v-for="e in employees" :key="e.empId" :value="e.empId">{{ e.empName }}</option>
</select>
```

---

## Task 3: 修复 LeaveView.vue 员工字段

**Files:**
- Modify: `frontend/src/views/LeaveView.vue`

- [ ] **Step 1: 修复 getEmpName 函数**

```typescript
const getEmpName = (empId: number) => {
  const emp = employees.value.find(e => e.empId === empId)
  return emp ? emp.empName : `员工${empId}`
}
```

- [ ] **Step 2: 修复员工下拉**

```vue
<select v-model="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required>
  <option :value="null" disabled>请选择员工</option>
  <option v-for="emp in employees" :key="emp.empId" :value="emp.empId">{{ emp.empName }}</option>
</select>
```

- [ ] **Step 3: 修复 fetchEmployees 数据解析**

确保 employees 从 `res.data.content` 读取（searchEmployees 返回分页结果）。

```typescript
const fetchEmployees = async () => {
  try {
    const res = await searchEmployees({ page: 0, size: 1000 })
    employees.value = res.data?.content || []
  } catch (e) {
    console.error('获取员工列表失败', e)
  }
}
```

- [ ] **Step 4: 将 alert 替换为 Toast**

在 script setup 顶部引入 ToastMessage：

```typescript
import ToastMessage from '@/components/ToastMessage.vue'
```

定义 toast：

```typescript
const toast = ref({ message: '', type: 'info' as 'success' | 'error' | 'info' })
const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  toast.value = { message, type }
}
```

将 `alert('保存失败，请重试')` 等替换为 `showToast('保存失败，请重试', 'error')`。

在 template 顶部添加：

```vue
<ToastMessage :message="toast.message" :type="toast.type" :duration="2600" />
```

---

## Task 4: 修复 OvertimeView.vue 员工字段

**Files:**
- Modify: `frontend/src/views/OvertimeView.vue`

- [ ] **Step 1: 修复 fetchEmployees 数据解析**

```typescript
const fetchEmployees = async () => {
  try {
    const res = await searchEmployees({ page: 0, size: 1000 })
    employees.value = res.data?.content || []
  } catch {
    employees.value = []
  }
}
```

- [ ] **Step 2: 修复 getEmpName 函数**

```typescript
const getEmpName = (empId: number) => {
  const emp = employees.value.find((e: any) => e.empId === empId)
  return emp ? emp.empName : empId
}
```

- [ ] **Step 3: 修复员工下拉**

```vue
<select v-model.number="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required>
  <option :value="null" disabled>请选择员工</option>
  <option v-for="emp in employees" :key="emp.empId" :value="emp.empId">{{ emp.empName }}</option>
</select>
```

- [ ] **Step 4: 添加 Toast 替换 alert/console.error**

参考 Task 3 引入 ToastMessage，将错误提示统一为 Toast。

---

## Task 5: 修复 AttendanceView.vue 员工字段和状态联动

**Files:**
- Modify: `frontend/src/views/AttendanceView.vue`

- [ ] **Step 1: 修复员工下拉和姓名显示**

```typescript
const getEmpName = (empId: number) => {
  const emp = employees.value.find(e => e.empId === empId)
  return emp ? emp.empName : `员工${empId}`
}
```

```vue
<select v-model="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required>
  <option :value="null" disabled>请选择员工</option>
  <option v-for="emp in employees" :key="emp.empId" :value="emp.empId">{{ emp.empName }}</option>
</select>
```

- [ ] **Step 2: 添加考勤状态联动逻辑**

监听 `form.attStatus` 变化：

```typescript
import { watch } from 'vue'

watch(() => form.value.attStatus, (status) => {
  if (status === '正常') {
    form.value.lateMinutes = 0
    form.value.earlyLeaveMinutes = 0
    form.value.absent = false
  } else if (status === '缺勤') {
    form.value.lateMinutes = 0
    form.value.earlyLeaveMinutes = 0
    form.value.absent = true
  }
})
```

- [ ] **Step 3: 添加 Toast 替换 alert**

参考 Task 3。

---

## Task 6: 修复 PerformanceView.vue 员工字段

**Files:**
- Modify: `frontend/src/views/PerformanceView.vue`

- [ ] **Step 1: 修复 getEmployeeName 函数**

```typescript
const getEmployeeName = (empId: number) => {
  const emp = employees.value.find(e => e.empId === empId)
  return emp ? emp.empName : `员工${empId}`
}
```

- [ ] **Step 2: 修复员工下拉**

```vue
<select v-model="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required>
  <option :value="null" disabled>请选择员工</option>
  <option v-for="emp in employees" :key="emp.empId" :value="emp.empId">{{ emp.empName }}</option>
</select>
```

- [ ] **Step 3: 添加 Toast 替换 alert/console.error**

参考 Task 3。

---

## Task 7: 检查 EmployeeView.vue 字段正确性

**Files:**
- Modify: `frontend/src/views/EmployeeView.vue`

- [ ] **Step 1: 确认下拉字段**

该视图当前已正确使用 `e.empId` / `e.empName`，无需修改。检查 `getDeptName` 和 `getPosName` 是否使用 `deptCode` / `posId` 正确匹配。

- [ ] **Step 2: 将 alert 替换为 Toast**

该视图当前使用 `alert()`，需要统一替换。引入 ToastMessage 组件并修改 `handleSave` 和 `handleDelete`。

---

## Task 8: 检查其他视图中的 alert 并替换

**Files:**
- Modify: `frontend/src/views/ProfileView.vue`
- Modify: `frontend/src/views/ProjectView.vue`
- Modify: `frontend/src/views/PaymentView.vue`
- Modify: `frontend/src/views/PositionChangeView.vue`
- Modify: `frontend/src/views/SystemView.vue`

- [ ] **Step 1: 搜索所有 alert 调用**

Run: `cd frontend && grep -r "alert(" src/views/`

- [ ] **Step 2: 逐个替换为 Toast**

每个视图引入 ToastMessage，定义 `toast` 和 `showToast`，将 `alert(message)` 替换为 `showToast(message, 'error')` 或 `'success'`。

---

## Task 9: 验证前端编译

**Files:**
- Run in: `frontend/`

- [ ] **Step 1: 类型检查**

Run: `cd frontend && npm run build`
Expected: 无 TypeScript 错误，构建成功

- [ ] **Step 2: 后端测试**

Run: `cd backend && mvn test -q`
Expected: 现有测试全部通过

---

## Self-Review

1. **Spec coverage**: 阶段一所有目标（字段修复、搜索修复、Toast 统一、考勤联动）均已覆盖。
2. **Placeholder scan**: 无 TBD/TODO，所有代码片段完整。
3. **Type consistency**: 统一使用 `empId`/`empName`，与后端实体一致。
