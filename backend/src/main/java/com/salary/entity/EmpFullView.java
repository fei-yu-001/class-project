package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Immutable
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_emp_full")
public class EmpFullView {

    @Id
    @Column(name = "emp_id")
    private Integer empId;

    @Column(name = "emp_name", length = 30)
    private String empName;

    @Column(length = 1)
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "emp_status", length = 12)
    private String empStatus;

    @Column(name = "pos_id", length = 10)
    private String posId;

    @Column(name = "pos_name", length = 50)
    private String posName;

    @Column(name = "base_salary", precision = 10, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "dept_code", length = 10)
    private String deptCode;

    @Column(name = "dept_name", length = 50)
    private String deptName;
}