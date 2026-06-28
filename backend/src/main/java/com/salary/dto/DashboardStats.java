package com.salary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    private long totalEmployees;
    private long activeEmployees;
    private long totalDepartments;
    private long totalPositions;
    private long pendingSalaries;
    private long paidSalaries;
    private BigDecimal currentPeriodGrossSalary;
    private BigDecimal currentPeriodNetSalary;
    private BigDecimal totalNetSalary;
    private long bankCardCount;
    private long alipayCount;
    private BigDecimal departmentCount;
    private BigDecimal positionCount;
    private BigDecimal totalPayment;

    private List<Map<String, Object>> monthlyNetPayChart;
    private List<Map<String, Object>> payTypeChart;
    private List<Map<String, Object>> gradeChart;

    // 个人数据（普通用户）
    private String myName;
    private BigDecimal myBaseSalary;
    private BigDecimal myLatestNetPay;
    private String myPerformanceGrade;
    private BigDecimal myTotalBonus;
    private BigDecimal myTotalDeduction;
}
