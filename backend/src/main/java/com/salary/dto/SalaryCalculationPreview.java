package com.salary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryCalculationPreview {

    private Integer empId;
    private String empName;
    private String payPeriod;
    private BigDecimal baseSalary;
    private BigDecimal performanceBonus;
    private BigDecimal fullAttendanceBonus;
    private BigDecimal overtimePay;
    private BigDecimal extraBonus;
    private BigDecimal extraDeduction;
    private BigDecimal taxDeduction;
    private BigDecimal insuranceDeduction;
    private BigDecimal attendanceDeduction;
    private BigDecimal leaveDeduction;
    private BigDecimal grossTotal;
    private BigDecimal deductTotal;
    private BigDecimal netPay;
    private String performanceGrade;
    private long lateCount;
    private long earlyLeaveCount;
    private long absenceCount;
    private BigDecimal approvedLeaveDays;
    private BigDecimal approvedOvertimeHours;
    private boolean generated;
    private String status;
}
