package com.salary.controller;

import com.salary.common.Result;
import com.salary.entity.*;
import com.salary.service.ViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/views")
@RequiredArgsConstructor
public class ViewController {

    private final ViewService viewService;

    @GetMapping("/emp-full")
    public Result<List<EmpFullView>> getEmpFull(@RequestParam(required = false) String deptCode) {
        if (deptCode != null) {
            return Result.success(viewService.getEmpFullByDept(deptCode));
        }
        return Result.success(viewService.getEmpFullInfo());
    }

    @GetMapping("/sal-detail")
    public Result<List<SalDetailView>> getSalDetails(@RequestParam(required = false) String payPeriod) {
        if (payPeriod != null) {
            return Result.success(viewService.getSalDetailsByPeriod(payPeriod));
        }
        return Result.success(viewService.getSalDetails());
    }

    @GetMapping("/emp-perf-att")
    public Result<List<EmpPerfAttView>> getEmpPerfAtt() {
        return Result.success(viewService.getEmpPerfAtt());
    }

    @GetMapping("/emp-project")
    public Result<List<EmpProjectView>> getEmpProjects() {
        return Result.success(viewService.getEmpProjects());
    }

    @GetMapping("/emp-payinfo")
    public Result<List<EmpPayInfoView>> getEmpPayInfo() {
        return Result.success(viewService.getEmpPayInfo());
    }

    @GetMapping("/schema")
    public Result<Map<String, Object>> getSchema() {
        return Result.success(viewService.getDatabaseSchema());
    }

    @GetMapping("/charts/monthly")
    public Result<List<Map<String, Object>>> getMonthlyChart() {
        return Result.success(viewService.getMonthlyNetPayChart());
    }

    @GetMapping("/charts/paytype")
    public Result<List<Map<String, Object>>> getPayTypeChart() {
        return Result.success(viewService.getPayTypeChart());
    }

    @GetMapping("/charts/grade")
    public Result<List<Map<String, Object>>> getGradeChart() {
        return Result.success(viewService.getGradeChart());
    }
}