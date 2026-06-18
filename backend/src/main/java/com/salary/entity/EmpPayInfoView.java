package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

@Data
@Entity
@Immutable
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_emp_payinfo")
public class EmpPayInfoView {

    @Id
    @Column(name = "emp_id")
    private Integer empId;

    @Column(name = "emp_name", length = 30)
    private String empName;

    @Column(name = "pay_type", length = 20)
    private String payType;

    @Column(name = "account_no", length = 100)
    private String accountNo;

    @Column(name = "card_type", length = 20)
    private String cardType;
}