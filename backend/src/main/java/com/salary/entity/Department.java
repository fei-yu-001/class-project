package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "department")
public class Department {

    @Id
    @Column(name = "dept_code", length = 10)
    private String deptCode;

    @Column(name = "dept_name", nullable = false, length = 50)
    private String deptName;

    @Column(name = "parent_dept_code", length = 10)
    private String parentDeptCode;

    @Column(name = "dept_manager_emp")
    private Integer deptManagerEmp;
}
