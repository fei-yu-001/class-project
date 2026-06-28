package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "overtime")
public class OvertimeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ot_id")
    private Integer otId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "ot_hours", nullable = false, precision = 4, scale = 1)
    private BigDecimal otHours;

    @Column(name = "ot_date", nullable = false)
    private LocalDate otDate;

    @Column(name = "ot_type", nullable = false, length = 20)
    private String otType;

    @Column(name = "approval_status", nullable = false, length = 20)
    private String approvalStatus;

    @PrePersist
    @PreUpdate
    protected void fillDefaults() {
        if (otHours == null) otHours = BigDecimal.ZERO;
        if (otDate == null) otDate = LocalDate.now();
        if (otType == null || otType.isBlank()) otType = "WEEKDAY";
        if (approvalStatus == null || approvalStatus.isBlank()) approvalStatus = "PENDING";
    }
}
