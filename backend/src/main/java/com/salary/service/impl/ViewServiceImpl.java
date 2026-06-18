package com.salary.service.impl;

import com.salary.entity.*;
import com.salary.repository.*;
import com.salary.service.ViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViewServiceImpl implements ViewService {

    private final EmpFullViewRepository empFullViewRepo;
    private final SalDetailViewRepository salDetailViewRepo;
    private final EmpPerfAttViewRepository empPerfAttViewRepo;
    private final EmpProjectViewRepository empProjectViewRepo;
    private final EmpPayInfoViewRepository empPayInfoViewRepo;

    @Override
    public List<EmpFullView> getEmpFullInfo() {
        return empFullViewRepo.findAll();
    }

    @Override
    public List<EmpFullView> getEmpFullByDept(String deptCode) {
        return empFullViewRepo.findByDeptCode(deptCode);
    }

    @Override
    public List<SalDetailView> getSalDetails() {
        return salDetailViewRepo.findAll();
    }

    @Override
    public List<SalDetailView> getSalDetailsByPeriod(String payPeriod) {
        return salDetailViewRepo.findByPayPeriod(payPeriod);
    }

    @Override
    public List<EmpPerfAttView> getEmpPerfAtt() {
        return empPerfAttViewRepo.findAll();
    }

    @Override
    public List<EmpProjectView> getEmpProjects() {
        return empProjectViewRepo.findAll();
    }

    @Override
    public List<EmpPayInfoView> getEmpPayInfo() {
        return empPayInfoViewRepo.findAll();
    }

    @Override
    public List<Map<String, Object>> getMonthlyNetPayChart() {
        List<Object[]> data = salDetailViewRepo.monthlyNetPayStats();
        if (data.isEmpty()) return Collections.emptyList();
        return data.stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("period", row[0]);
            Object val = row[1];
            m.put("netPay", val instanceof BigDecimal ? ((BigDecimal) val).doubleValue() : ((Number) val).doubleValue());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getPayTypeChart() {
        return empPayInfoViewRepo.payTypeDistribution().stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", row[0]);
            m.put("count", ((Number) row[1]).intValue());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getGradeChart() {
        return empPerfAttViewRepo.gradeDistribution().stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("grade", row[0]);
            m.put("count", ((Number) row[1]).intValue());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDatabaseSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();

        List<Map<String, Object>> tables = new ArrayList<>();
        tables.add(table("department", "部门表", "dept_code", List.of("部门代码", "部门名称", "上级部门代码", "部门负责人工号"), List.of("自引用: parent_dept_code → department", "引用: dept_manager_emp → employee"), "ON DELETE SET NULL / ON UPDATE CASCADE"));
        tables.add(table("position", "职位表", "pos_id", List.of("职位ID", "职位名称", "基本工资", "所属部门代码"), List.of("dept_code → department"), "ON DELETE RESTRICT / ON UPDATE CASCADE"));
        tables.add(table("employee", "员工表", "emp_id (自增)", List.of("工号", "姓名", "性别", "出生日期", "入职日期", "在职状态", "现任职位ID", "所属部门代码"), List.of("pos_id → position", "dept_code → department"), "ON DELETE RESTRICT / ON UPDATE CASCADE"));
        tables.add(table("users", "用户表", "id (自增)", List.of("ID", "用户名(唯一)", "密码", "昵称", "关联员工ID", "角色", "启用状态", "头像", "创建时间", "更新时间"), List.of("emp_id → employee"), "ON DELETE SET NULL"));
        tables.add(table("payment_method", "支付方式表", "emp_id", List.of("员工工号", "支付方式类型", "账号", "卡类型"), List.of("emp_id → employee"), "ON DELETE CASCADE"));
        tables.add(table("project", "项目表", "proj_id (自增)", List.of("项目ID", "项目名称", "项目状态"), List.of(), ""));
        tables.add(table("project_member", "项目成员中间表", "emp_id, proj_id", List.of("员工工号", "项目ID", "角色", "贡献系数(0~1)"), List.of("emp_id → employee", "proj_id → project"), ""));
        tables.add(table("performance", "绩效考核表", "review_id (自增)", List.of("考核ID", "员工工号", "绩效等级"), List.of("emp_id → employee"), "ON DELETE CASCADE"));
        tables.add(table("attendance", "考勤记录表", "att_id (自增)", List.of("考勤ID", "员工工号", "考勤状态"), List.of("emp_id → employee"), "ON DELETE CASCADE"));
        tables.add(table("leave_req", "请假申请表", "leave_id (自增)", List.of("请假ID", "员工工号", "请假类型"), List.of("emp_id → employee"), "ON DELETE CASCADE"));
        tables.add(table("overtime", "加班记录表", "ot_id (自增)", List.of("加班ID", "员工工号", "加班时长"), List.of("emp_id → employee"), "ON DELETE CASCADE"));
        tables.add(table("pos_change", "职位变动记录表", "change_id (自增)", List.of("变动ID", "员工工号", "变动原因"), List.of("emp_id → employee"), "ON DELETE CASCADE"));
        tables.add(table("salary_record", "工资记录表", "salary_id (自增)", List.of("工资ID", "员工工号", "基本工资快照", "计薪周期", "应发合计", "扣款合计", "实发工资"), List.of("emp_id → employee"), ""));
        tables.add(table("bonus + sal_bonus_rel", "奖金表 + 关联表", "bonus_id / salary_id+bonus_id", List.of("奖金编号", "奖金类型", "税前金额", "→ 关联salary_record"), List.of("salary_id → salary_record", "bonus_id → bonus"), "M:N 关系"));
        tables.add(table("deduction + sal_deduct_rel", "扣款表 + 关联表", "deduct_id / salary_id+deduct_id", List.of("扣款编号", "扣款类型", "金额", "→ 关联salary_record"), List.of("salary_id → salary_record", "deduct_id → deduction"), "M:N 关系"));
        schema.put("tables", tables);

        List<Map<String, Object>> views = new ArrayList<>();
        views.add(view("v_emp_full", "员工全信息视图", "EMPLOYEE ⟂ POSITION ⟂ DEPARTMENT", "员工+职位+部门三表JOIN，展示员工基本工资、所属部门等核心信息"));
        views.add(view("v_sal_detail", "工资明细视图", "SALARY_RECORD ⟂ EMPLOYEE ⟂ BONUS ⟂ DEDUCTION", "工资+员工+奖金+扣款四表LEFT JOIN，展示每笔工资的奖金和扣款明细"));
        views.add(view("v_emp_perf_att", "员工考勤绩效视图", "EMPLOYEE ⟂ PERFORMANCE ⟂ ATTENDANCE", "员工+绩效+考勤三表LEFT JOIN，支持按绩效等级/考勤状态筛选"));
        views.add(view("v_emp_project", "员工项目参与视图", "EMPLOYEE ⟂ PROJECT_MEMBER ⟂ PROJECT", "通过中间表project_member展示M:N关系，含角色和贡献系数"));
        views.add(view("v_emp_payinfo", "员工薪资结算账户视图", "EMPLOYEE ⟂ PAYMENT_METHOD", "1:1关系，展示每个员工的支付方式（银行卡/支付宝）"));
        schema.put("views", views);

        List<Map<String, String>> indexes = new ArrayList<>();
        indexes.add(index("idx_dept_name", "department", "dept_name", "按部门名称查询"));
        indexes.add(index("idx_pos_name", "position", "pos_name", "按职位名称查询"));
        indexes.add(index("idx_emp_name", "employee", "emp_name", "按员工姓名搜索"));
        indexes.add(index("idx_emp_status", "employee", "emp_status", "按在职状态筛选"));
        indexes.add(index("idx_sal_period", "salary_record", "pay_period", "按计薪周期查询"));
        indexes.add(index("idx_bonus_type", "bonus", "bonus_type", "按奖金类型分类"));
        indexes.add(index("idx_deduct_type", "deduction", "deduct_type", "按扣款类型分类"));
        indexes.add(index("idx_proj_name", "project", "proj_name", "按项目名称搜索"));
        indexes.add(index("idx_perf_emp", "performance", "emp_id", "加速员工绩效JOIN"));
        indexes.add(index("idx_att_emp", "attendance", "emp_id", "加速员工考勤JOIN"));
        indexes.add(index("idx_leave_emp", "leave_req", "emp_id", "加速员工请假JOIN"));
        indexes.add(index("idx_ot_emp", "overtime", "emp_id", "加速员工加班JOIN"));
        indexes.add(index("idx_change_emp", "pos_change", "emp_id", "加速员工职位变动JOIN"));
        indexes.add(index("idx_pay_emp", "payment_method", "emp_id", "加速员工支付方式JOIN"));
        schema.put("indexes", indexes);

        return schema;
    }

    private Map<String, Object> table(String code, String name, String pk, List<String> columns, List<String> fks, String cascade) {
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("code", code);
        t.put("name", name);
        t.put("primaryKey", pk);
        t.put("columns", columns);
        t.put("foreignKeys", fks);
        t.put("cascade", cascade);
        return t;
    }

    private Map<String, Object> view(String code, String name, String joins, String desc) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("code", code);
        v.put("name", name);
        v.put("joins", joins);
        v.put("description", desc);
        return v;
    }

    private Map<String, String> index(String name, String table, String column, String purpose) {
        Map<String, String> i = new LinkedHashMap<>();
        i.put("name", name);
        i.put("table", table);
        i.put("column", column);
        i.put("purpose", purpose);
        return i;
    }
}