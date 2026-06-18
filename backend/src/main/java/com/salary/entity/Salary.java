package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "salary_record")
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salary_id")
    private Integer salaryId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "base_snap", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseSnap;

    @Column(name = "pay_period", nullable = false, length = 7)
    private String payPeriod;

    @Column(name = "gross_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal grossTotal;

    @Column(name = "deduct_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal deductTotal;

    @Column(name = "net_pay", nullable = false, precision = 10, scale = 2)
    private BigDecimal netPay;

    @Column(name = "performance_bonus", nullable = false, precision = 10, scale = 2)
    private BigDecimal performanceBonus;

    @Column(name = "full_attendance_bonus", nullable = false, precision = 10, scale = 2)
    private BigDecimal fullAttendanceBonus;

    @Column(name = "overtime_pay", nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimePay;

    @Column(name = "leave_deduction", nullable = false, precision = 10, scale = 2)
    private BigDecimal leaveDeduction;

    @Column(name = "attendance_deduction", nullable = false, precision = 10, scale = 2)
    private BigDecimal attendanceDeduction;

    @Column(name = "tax_deduction", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxDeduction;

    @Column(name = "insurance_deduction", nullable = false, precision = 10, scale = 2)
    private BigDecimal insuranceDeduction;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Transient
    private List<Bonus> bonuses;

    @Transient
    private List<Deduction> deductions;

    @PrePersist
    @PreUpdate
    protected void onCreate() {
        if (grossTotal == null) grossTotal = BigDecimal.ZERO;
        if (deductTotal == null) deductTotal = BigDecimal.ZERO;
        if (netPay == null) netPay = BigDecimal.ZERO;
        if (performanceBonus == null) performanceBonus = BigDecimal.ZERO;
        if (fullAttendanceBonus == null) fullAttendanceBonus = BigDecimal.ZERO;
        if (overtimePay == null) overtimePay = BigDecimal.ZERO;
        if (leaveDeduction == null) leaveDeduction = BigDecimal.ZERO;
        if (attendanceDeduction == null) attendanceDeduction = BigDecimal.ZERO;
        if (taxDeduction == null) taxDeduction = BigDecimal.ZERO;
        if (insuranceDeduction == null) insuranceDeduction = BigDecimal.ZERO;
        if (status == null || status.isBlank()) status = "GENERATED";
        if (generatedAt == null) generatedAt = LocalDateTime.now();
    }
}
