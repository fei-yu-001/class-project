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
@Table(name = "bonus")
public class Bonus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bonus_id")
    private Integer bonusId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "pay_period", nullable = false, length = 7)
    private String payPeriod;

    @Column(name = "bonus_type", nullable = false, length = 20)
    private String bonusType;

    @Column(name = "pre_tax_amt", nullable = false, precision = 10, scale = 2)
    private BigDecimal preTaxAmt;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (preTaxAmt == null) preTaxAmt = BigDecimal.ZERO;
    }
}
