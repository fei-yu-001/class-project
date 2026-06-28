package com.salary.service.impl;

import com.salary.entity.PerformanceReview;
import com.salary.repository.PerformanceReviewRepository;
import com.salary.service.PerformanceReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceReviewServiceImpl implements PerformanceReviewService {
    private final PerformanceReviewRepository performanceReviewRepository;

    @Override
    public List<PerformanceReview> getAllReviews() { return performanceReviewRepository.findAll(); }

    @Override
    public PerformanceReview getReviewById(Integer id) { return performanceReviewRepository.findById(id).orElse(null); }

    @Override
    public List<PerformanceReview> getReviewsByEmployee(Integer empId) { return performanceReviewRepository.findByEmpId(empId); }

    @Override
    @Transactional
    public PerformanceReview createReview(PerformanceReview review) {
        if (review.getEmpId() != null && review.getReviewPeriod() != null) {
            var existing = performanceReviewRepository
                .findFirstByEmpIdAndReviewPeriodOrderByReviewIdDesc(review.getEmpId(), review.getReviewPeriod());
            if (existing.isPresent()) {
                throw new IllegalArgumentException("该员工在 " + review.getReviewPeriod() + " 已有考核记录，请编辑而非重复创建");
            }
        }
        return performanceReviewRepository.save(review);
    }

    @Override
    @Transactional
    public PerformanceReview updateReview(Integer id, PerformanceReview review) {
        PerformanceReview existing = performanceReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("考核记录不存在"));
        existing.setEmpId(review.getEmpId());
        existing.setGrade(review.getGrade());
        existing.setReviewPeriod(review.getReviewPeriod());
        return performanceReviewRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteReview(Integer id) { performanceReviewRepository.deleteById(id); }
}
