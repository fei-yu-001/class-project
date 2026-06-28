package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "leave_balance",
    uniqueConstraints = @UniqueConstraint(columnNames = {"emp_id", "leave_type", "year"}))
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Integer balanceId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "leave_type", nullable = false, length = 20)
    private String leaveType;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "total_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalDays;

    @Column(name = "used_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal usedDays;

    @PrePersist
    protected void onCreate() {
        if (usedDays == null) usedDays = BigDecimal.ZERO;
        if (totalDays == null) totalDays = BigDecimal.ZERO;
    }
}
