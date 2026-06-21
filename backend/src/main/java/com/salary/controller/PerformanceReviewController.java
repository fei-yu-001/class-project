package com.salary.controller;

import com.salary.common.Result;
import com.salary.entity.PerformanceReview;
import com.salary.security.RequireRole;
import com.salary.service.PerformanceReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/performance-reviews")
@RequiredArgsConstructor
public class PerformanceReviewController {
    private final PerformanceReviewService performanceReviewService;

    @GetMapping
    public Result<List<PerformanceReview>> getAll() { return Result.success(performanceReviewService.getAllReviews()); }

    @GetMapping("/{id}")
    public Result<PerformanceReview> getById(@PathVariable Integer id) { return Result.success(performanceReviewService.getReviewById(id)); }

    @GetMapping("/employee/{empId}")
    public Result<List<PerformanceReview>> getByEmployee(@PathVariable Integer empId) { return Result.success(performanceReviewService.getReviewsByEmployee(empId)); }

    @PostMapping
    @RequireRole("ADMIN")
    public Result<PerformanceReview> create(@RequestBody PerformanceReview review) { return Result.success(performanceReviewService.createReview(review)); }

    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<PerformanceReview> update(@PathVariable Integer id, @RequestBody PerformanceReview review) { return Result.success(performanceReviewService.updateReview(id, review)); }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<Void> delete(@PathVariable Integer id) { performanceReviewService.deleteReview(id); return Result.success(); }
}
