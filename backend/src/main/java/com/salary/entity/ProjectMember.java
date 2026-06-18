package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "project_member")
@IdClass(ProjectMember.ProjectMemberId.class)
public class ProjectMember {

    @Id
    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Id
    @Column(name = "proj_id", nullable = false)
    private Integer projId;

    @Column(name = "role_name", length = 30)
    private String roleName;

    @Column(name = "contrib_coeff", nullable = false, precision = 3, scale = 2)
    private BigDecimal contribCoeff;

    @PrePersist
    protected void onCreate() {
        if (contribCoeff == null) contribCoeff = BigDecimal.ONE;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectMemberId implements Serializable {
        private Integer empId;
        private Integer projId;
    }
}
