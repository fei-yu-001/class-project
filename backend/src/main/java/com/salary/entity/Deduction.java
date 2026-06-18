package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    @Column(name = "deduct_type", nullable = false, length = 20)
    private String deductType;

    @Column(name = "deduct_amt", nullable = false, precision = 10, scale = 2)
    private BigDecimal deductAmt;
}
