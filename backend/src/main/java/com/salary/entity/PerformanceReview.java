package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "performance")
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "grade", nullable = false, length = 10)
    private String grade;

    @Column(name = "review_period", nullable = false, length = 7)
    private String reviewPeriod;

    @PrePersist
    @PreUpdate
    protected void fillDefaults() {
        if (reviewPeriod == null || reviewPeriod.isBlank()) {
            reviewPeriod = java.time.YearMonth.now().toString();
        }
    }
}
