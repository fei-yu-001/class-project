package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    @Column(name = "bonus_type", nullable = false, length = 20)
    private String bonusType;

    @Column(name = "pre_tax_amt", nullable = false, precision = 10, scale = 2)
    private BigDecimal preTaxAmt;
}
