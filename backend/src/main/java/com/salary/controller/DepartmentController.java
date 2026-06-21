package com.salary.controller;

import com.salary.common.Result;
import com.salary.dto.DepartmentRequest;
import com.salary.entity.Department;
import com.salary.security.RequireRole;
import com.salary.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @RequireRole("ADMIN")
    public Result<Department> create(@Valid @RequestBody DepartmentRequest request) {
        return Result.success(departmentService.create(request));
    }

    @PutMapping("/{deptCode}")
    @RequireRole("ADMIN")
    public Result<Department> update(@PathVariable String deptCode, @Valid @RequestBody DepartmentRequest request) {
        return Result.success(departmentService.update(deptCode, request));
    }

    @DeleteMapping("/{deptCode}")
    @RequireRole("ADMIN")
    public Result<Void> delete(@PathVariable String deptCode) {
        departmentService.delete(deptCode);
        return Result.success();
    }

    @GetMapping("/{deptCode}")
    public Result<Department> getById(@PathVariable String deptCode) {
        return Result.success(departmentService.getById(deptCode));
    }

    @GetMapping("/list")
    public Result<List<Department>> listAll() {
        return Result.success(departmentService.listAll());
    }

    @GetMapping("/page")
    public Result<Page<Department>> listPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(departmentService.listPage(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "deptCode"))));
    }
}
