package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pos_change")
public class PositionChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "change_id")
    private Integer changeId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "old_pos_id", length = 10)
    private String oldPosId;

    @Column(name = "new_pos_id", length = 10)
    private String newPosId;

    @Column(name = "change_date")
    private LocalDate changeDate;

    @Column(name = "change_reason", length = 100)
    private String changeReason;
}
