package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_method")
public class PaymentMethod {

    @Id
    @Column(name = "emp_id")
    private Integer empId;

    @Column(name = "pay_type", nullable = false, length = 20)
    private String payType;

    @Column(name = "account_no", nullable = false, length = 100)
    private String accountNo;

    @Column(name = "card_type", length = 20)
    private String cardType;
}
