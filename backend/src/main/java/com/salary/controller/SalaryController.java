package com.salary.controller;

import com.salary.common.Result;
import com.salary.dto.SalaryBatchGenerateRequest;
import com.salary.dto.SalaryCalculationPreview;
import com.salary.dto.SalaryRequest;
import com.salary.entity.Salary;
import com.salary.security.RequireRole;
import com.salary.service.SalaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/salaries")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    @PostMapping
    @RequireRole("ADMIN")
    public Result<Salary> create(@Valid @RequestBody SalaryRequest request) {
        return Result.success(salaryService.create(request));
    }

    @PutMapping("/{salaryId}")
    @RequireRole("ADMIN")
    public Result<Salary> update(@PathVariable Integer salaryId, @Valid @RequestBody SalaryRequest request) {
        return Result.success(salaryService.update(salaryId, request));
    }

    @DeleteMapping("/{salaryId}")
    @RequireRole("ADMIN")
    public Result<Void> delete(@PathVariable Integer salaryId) {
        salaryService.delete(salaryId);
        return Result.success();
    }

    @GetMapping("/{salaryId}")
    @RequireRole("ADMIN")
    public Result<Salary> getById(@PathVariable Integer salaryId) {
        return Result.success(salaryService.getById(salaryId));
    }

    @GetMapping("/employee/{empId}")
    @RequireRole("ADMIN")
    public Result<List<Salary>> getByEmployee(@PathVariable Integer empId) {
        return Result.success(salaryService.getByEmployee(empId));
    }

    @GetMapping("/search")
    @RequireRole("ADMIN")
    public Result<Page<Salary>> search(
            @RequestParam(required = false) Integer empId,
            @RequestParam(required = false) String payPeriod,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(salaryService.search(empId, payPeriod,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "salaryId"))));
    }

    @GetMapping("/preview")
    @RequireRole("ADMIN")
    public Result<List<SalaryCalculationPreview>> preview(@RequestParam String payPeriod) {
        return Result.success(salaryService.preview(payPeriod));
    }

    @PostMapping("/generate")
    @RequireRole("ADMIN")
    public Result<List<Salary>> generate(@Valid @RequestBody SalaryBatchGenerateRequest request) {
        return Result.success("工资已生成", salaryService.generateBatch(request));
    }

    @PostMapping("/{salaryId}/approve")
    @RequireRole("ADMIN")
    public Result<Salary> approve(@PathVariable Integer salaryId) {
        return Result.success("工资已审核", salaryService.approve(salaryId));
    }

    @PostMapping("/{salaryId}/pay")
    @RequireRole("ADMIN")
    public Result<Salary> pay(@PathVariable Integer salaryId) {
        return Result.success("工资已发放", salaryService.pay(salaryId));
    }

    @PostMapping("/batch-approve")
    @RequireRole("ADMIN")
    public Result<List<Salary>> batchApprove(@RequestBody Map<String, List<Integer>> body) {
        List<Integer> salaryIds = body.get("salaryIds");
        if (salaryIds == null || salaryIds.isEmpty()) {
            return Result.badRequest("请选择要审核的工资记录");
        }
        return Result.success("批量审核成功", salaryService.batchApprove(salaryIds));
    }

    @PostMapping("/batch-pay")
    @RequireRole("ADMIN")
    public Result<List<Salary>> batchPay(@RequestBody Map<String, List<Integer>> body) {
        List<Integer> salaryIds = body.get("salaryIds");
        if (salaryIds == null || salaryIds.isEmpty()) {
            return Result.badRequest("请选择要发放的工资记录");
        }
        return Result.success("批量发放成功", salaryService.batchPay(salaryIds));
    }
}
