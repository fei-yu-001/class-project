package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "att_id")
    private Integer attId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "att_date", nullable = false)
    private LocalDate attDate;

    @Column(name = "att_status", nullable = false, length = 20)
    private String attStatus;

    @Column(name = "late_minutes", nullable = false)
    private Integer lateMinutes;

    @Column(name = "early_leave_minutes", nullable = false)
    private Integer earlyLeaveMinutes;

    @Column(name = "absent", nullable = false)
    private Boolean absent;

    @PrePersist
    @PreUpdate
    protected void fillDefaults() {
        if (attDate == null) attDate = LocalDate.now();
        if (attStatus == null || attStatus.isBlank()) attStatus = "正常";
        if (lateMinutes == null) lateMinutes = "迟到".equals(attStatus) ? 1 : 0;
        if (earlyLeaveMinutes == null) earlyLeaveMinutes = "早退".equals(attStatus) ? 1 : 0;
        if (absent == null) absent = "缺勤".equals(attStatus);
    }
}
