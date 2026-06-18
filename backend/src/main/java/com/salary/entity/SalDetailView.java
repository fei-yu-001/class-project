package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Data
@Entity
@Immutable
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_sal_detail")
public class SalDetailView {

    @Id
    @Column(name = "salary_id")
    private Integer salaryId;

    @Column(name = "pay_period", length = 7)
    private String payPeriod;

    @Column(name = "gross_total", precision = 10, scale = 2)
    private BigDecimal grossTotal;

    @Column(name = "deduct_total", precision = 10, scale = 2)
    private BigDecimal deductTotal;

    @Column(name = "net_pay", precision = 10, scale = 2)
    private BigDecimal netPay;

    @Column(name = "emp_id")
    private Integer empId;

    @Column(name = "emp_name", length = 30)
    private String empName;

    @Column(name = "bonus_id")
    private Integer bonusId;

    @Column(name = "bonus_type", length = 20)
    private String bonusType;

    @Column(name = "pre_tax_amt", precision = 10, scale = 2)
    private BigDecimal preTaxAmt;

    @Column(name = "deduct_id")
    private Integer deductId;

    @Column(name = "deduct_type", length = 20)
    private String deductType;

    @Column(name = "deduct_amt", precision = 10, scale = 2)
    private BigDecimal deductAmt;
}