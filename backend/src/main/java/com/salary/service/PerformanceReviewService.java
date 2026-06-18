package com.salary.service;

import com.salary.entity.PerformanceReview;
import java.util.List;

public interface PerformanceReviewService {
    List<PerformanceReview> getAllReviews();
    PerformanceReview getReviewById(Integer id);
    List<PerformanceReview> getReviewsByEmployee(Integer empId);
    PerformanceReview createReview(PerformanceReview review);
    PerformanceReview updateReview(Integer id, PerformanceReview review);
    void deleteReview(Integer id);
}
