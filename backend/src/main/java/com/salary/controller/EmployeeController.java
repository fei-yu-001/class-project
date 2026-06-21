package com.salary.controller;

import com.salary.common.Result;
import com.salary.dto.EmployeeRequest;
import com.salary.entity.Employee;
import com.salary.security.RequireRole;
import com.salary.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @RequireRole("ADMIN")
    public Result<Employee> create(@Valid @RequestBody EmployeeRequest request) {
        return Result.success(employeeService.create(request));
    }

    @PutMapping("/{empId}")
    @RequireRole("ADMIN")
    public Result<Employee> update(@PathVariable Integer empId, @Valid @RequestBody EmployeeRequest request) {
        return Result.success(employeeService.update(empId, request));
    }

    @DeleteMapping("/{empId}")
    @RequireRole("ADMIN")
    public Result<Void> delete(@PathVariable Integer empId) {
        employeeService.delete(empId);
        return Result.success();
    }

    @GetMapping("/{empId}")
    public Result<Employee> getById(@PathVariable Integer empId) {
        return Result.success(employeeService.getById(empId));
    }

    @GetMapping("/search")
    public Result<Page<Employee>> search(
            @RequestParam(required = false) String empName,
            @RequestParam(required = false) String deptCode,
            @RequestParam(required = false) String empStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(employeeService.search(empName, deptCode, empStatus,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "empId"))));
    }
}
