package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "position")
public class Position {

    @Id
    @Column(name = "pos_id", length = 10)
    private String posId;

    @Column(name = "pos_name", nullable = false, length = 50)
    private String posName;

    @Column(name = "base_salary", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "dept_code", nullable = false, length = 10)
    private String deptCode;
}
