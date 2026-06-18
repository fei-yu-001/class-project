package com.salary.controller;

import com.salary.common.Result;
import com.salary.entity.PositionChange;
import com.salary.security.RequireRole;
import com.salary.service.PositionChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/position-changes")
@RequiredArgsConstructor
public class PositionChangeController {
    private final PositionChangeService positionChangeService;

    @GetMapping
    public Result<List<PositionChange>> getAll() { return Result.success(positionChangeService.getAllChanges()); }

    @GetMapping("/employee/{empId}")
    public Result<List<PositionChange>> getByEmployee(@PathVariable Integer empId) { return Result.success(positionChangeService.getChangesByEmployee(empId)); }

    @PostMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<PositionChange> create(@RequestBody PositionChange change) { return Result.success(positionChangeService.createChange(change)); }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<Void> delete(@PathVariable Integer id) { positionChangeService.deleteChange(id); return Result.success(); }
}
