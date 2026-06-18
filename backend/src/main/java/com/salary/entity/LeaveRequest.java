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
@Table(name = "leave_req")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_id")
    private Integer leaveId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "leave_type", nullable = false, length = 20)
    private String leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "leave_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal leaveDays;

    @Column(name = "approval_status", nullable = false, length = 20)
    private String approvalStatus;

    @PrePersist
    @PreUpdate
    protected void fillDefaults() {
        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = startDate;
        if (leaveDays == null) leaveDays = BigDecimal.ONE;
        if (approvalStatus == null || approvalStatus.isBlank()) approvalStatus = "APPROVED";
    }
}
