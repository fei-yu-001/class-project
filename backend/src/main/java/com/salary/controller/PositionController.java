package com.salary.controller;

import com.salary.common.Result;
import com.salary.dto.PositionRequest;
import com.salary.entity.Position;
import com.salary.security.RequireRole;
import com.salary.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<Position> create(@Valid @RequestBody PositionRequest request) {
        return Result.success(positionService.create(request));
    }

    @PutMapping("/{posId}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<Position> update(@PathVariable String posId, @Valid @RequestBody PositionRequest request) {
        return Result.success(positionService.update(posId, request));
    }

    @DeleteMapping("/{posId}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<Void> delete(@PathVariable String posId) {
        positionService.delete(posId);
        return Result.success();
    }

    @GetMapping("/{posId}")
    public Result<Position> getById(@PathVariable String posId) {
        return Result.success(positionService.getById(posId));
    }

    @GetMapping("/list")
    public Result<List<Position>> listAll() {
        return Result.success(positionService.listAll());
    }

    @GetMapping("/department/{deptCode}")
    public Result<List<Position>> listByDepartment(@PathVariable String deptCode) {
        return Result.success(positionService.listByDeptCode(deptCode));
    }

    @GetMapping("/page")
    public Result<Page<Position>> listPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(positionService.listPage(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "posId"))));
    }
}
