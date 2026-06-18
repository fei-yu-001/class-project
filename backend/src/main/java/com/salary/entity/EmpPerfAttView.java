package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

@Data
@Entity
@Immutable
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_emp_perf_att")
public class EmpPerfAttView {

    @Id
    @Column(name = "emp_id")
    private Integer empId;

    @Column(name = "emp_name", length = 30)
    private String empName;

    @Column(length = 10)
    private String grade;

    @Column(name = "att_status", length = 20)
    private String attStatus;
}