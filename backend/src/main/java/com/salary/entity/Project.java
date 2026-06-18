package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proj_id")
    private Integer projId;

    @Column(name = "proj_name", nullable = false, length = 100)
    private String projName;

    @Column(name = "proj_status", nullable = false, length = 20)
    private String projStatus;

    @PrePersist
    protected void onCreate() {
        if (projStatus == null) projStatus = "进行中";
    }
}
