package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "deduction")
public class Deduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deduct_id")
    private Integer deductId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "pay_period", nullable = false, length = 7)
    private String payPeriod;

    @Column(name = "deduct_type", nullable = false, length = 20)
    private String deductType;

    @Column(name = "deduct_amt", nullable = false, precision = 10, scale = 2)
    private BigDecimal deductAmt;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (deductAmt == null) deductAmt = BigDecimal.ZERO;
    }
}
