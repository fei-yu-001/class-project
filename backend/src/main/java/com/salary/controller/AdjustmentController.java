package com.salary.controller;

import com.salary.common.Result;
import com.salary.dto.AdjustmentRequest;
import com.salary.entity.Bonus;
import com.salary.entity.Deduction;
import com.salary.security.RequireRole;
import com.salary.service.AdjustmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adjustments")
@RequiredArgsConstructor
public class AdjustmentController {

    private final AdjustmentService adjustmentService;

    @GetMapping("/bonuses")
    @RequireRole("ADMIN")
    public Result<List<Bonus>> listBonuses(
            @RequestParam(required = false) Integer empId,
            @RequestParam(required = false) String payPeriod) {
        return Result.success(adjustmentService.listBonuses(empId, payPeriod));
    }

    @PostMapping("/bonuses")
    @RequireRole("ADMIN")
    public Result<Bonus> createBonus(@Valid @RequestBody AdjustmentRequest request) {
        return Result.success("奖金已添加", adjustmentService.createBonus(request));
    }

    @DeleteMapping("/bonuses/{bonusId}")
    @RequireRole("ADMIN")
    public Result<Void> deleteBonus(@PathVariable Integer bonusId) {
        adjustmentService.deleteBonus(bonusId);
        return Result.success();
    }

    @GetMapping("/deductions")
    @RequireRole("ADMIN")
    public Result<List<Deduction>> listDeductions(
            @RequestParam(required = false) Integer empId,
            @RequestParam(required = false) String payPeriod) {
        return Result.success(adjustmentService.listDeductions(empId, payPeriod));
    }

    @PostMapping("/deductions")
    @RequireRole("ADMIN")
    public Result<Deduction> createDeduction(@Valid @RequestBody AdjustmentRequest request) {
        return Result.success("罚款已添加", adjustmentService.createDeduction(request));
    }

    @DeleteMapping("/deductions/{deductId}")
    @RequireRole("ADMIN")
    public Result<Void> deleteDeduction(@PathVariable Integer deductId) {
        adjustmentService.deleteDeduction(deductId);
        return Result.success();
    }
}
