package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sal_bonus_rel")
@IdClass(SalBonusRel.SalBonusRelId.class)
public class SalBonusRel {

    @Id
    @Column(name = "salary_id", nullable = false)
    private Integer salaryId;

    @Id
    @Column(name = "bonus_id", nullable = false)
    private Integer bonusId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalBonusRelId implements Serializable {
        private Integer salaryId;
        private Integer bonusId;
    }
}
