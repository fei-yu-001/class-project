package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sal_deduct_rel")
@IdClass(SalDeductRel.SalDeductRelId.class)
public class SalDeductRel {

    @Id
    @Column(name = "salary_id", nullable = false)
    private Integer salaryId;

    @Id
    @Column(name = "deduct_id", nullable = false)
    private Integer deductId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalDeductRelId implements Serializable {
        private Integer salaryId;
        private Integer deductId;
    }
}
