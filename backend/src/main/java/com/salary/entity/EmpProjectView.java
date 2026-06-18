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
@Table(name = "v_emp_project")
public class EmpProjectView {

    @Id
    @Column(name = "emp_id")
    private Integer empId;

    @Column(name = "emp_name", length = 30)
    private String empName;

    @Column(name = "proj_id")
    private Integer projId;

    @Column(name = "proj_name", length = 100)
    private String projName;

    @Column(name = "role_name", length = 30)
    private String roleName;

    @Column(name = "contrib_coeff", precision = 3, scale = 2)
    private BigDecimal contribCoeff;
}