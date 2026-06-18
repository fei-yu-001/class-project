package com.salary.repository;

import com.salary.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Integer> {
    List<PerformanceReview> findByEmpId(Integer empId);

    Optional<PerformanceReview> findFirstByEmpIdAndReviewPeriodOrderByReviewIdDesc(Integer empId, String reviewPeriod);
}
