package com.salary.controller;

import com.salary.common.Result;
import com.salary.dto.PaymentMethodRequest;
import com.salary.entity.PaymentMethod;
import com.salary.security.RequireRole;
import com.salary.service.PaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @PostMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<PaymentMethod> create(@Valid @RequestBody PaymentMethodRequest request) {
        return Result.success(paymentMethodService.create(request));
    }

    @PutMapping("/{empId}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<PaymentMethod> update(@PathVariable Integer empId, @Valid @RequestBody PaymentMethodRequest request) {
        return Result.success(paymentMethodService.update(empId, request));
    }

    @DeleteMapping("/{empId}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<Void> delete(@PathVariable Integer empId) {
        paymentMethodService.delete(empId);
        return Result.success();
    }

    @GetMapping("/{empId}")
    public Result<PaymentMethod> getById(@PathVariable Integer empId) {
        return Result.success(paymentMethodService.getById(empId));
    }

    @GetMapping("/employee/{empId}")
    public Result<PaymentMethod> getByEmployeeId(@PathVariable Integer empId) {
        return Result.success(paymentMethodService.getByEmpId(empId));
    }

    @GetMapping("/list")
    public Result<List<PaymentMethod>> listAll() {
        return Result.success(paymentMethodService.listAll());
    }
}
