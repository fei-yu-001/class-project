package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_id")
    private Integer empId;

    @Column(name = "emp_name", nullable = false, length = 30)
    private String empName;

    @Column(length = 1)
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "emp_status", nullable = false, length = 12)
    private String empStatus;

    @Column(name = "pos_id", nullable = false, length = 10)
    private String posId;

    @Column(name = "dept_code", nullable = false, length = 10)
    private String deptCode;

    @PrePersist
    protected void onCreate() {
        if (gender == null) gender = "男";
        if (empStatus == null) empStatus = "在职";
    }
}
