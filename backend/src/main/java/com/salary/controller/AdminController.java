package com.salary.controller;

import com.salary.common.Result;
import com.salary.entity.User;
import com.salary.security.RequireRole;
import com.salary.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @RequireRole("SUPER_ADMIN")
    public Result<List<User>> listUsers() {
        return Result.success(adminService.listUsers());
    }

    @PutMapping("/users/{id}/role")
    @RequireRole("SUPER_ADMIN")
    public Result<User> updateUserRole(@PathVariable Integer id, @RequestParam String role) {
        try {
            return Result.success(adminService.updateUserRole(id, role));
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @PutMapping("/users/{id}/status")
    @RequireRole("SUPER_ADMIN")
    public Result<User> updateUserStatus(@PathVariable Integer id, @RequestParam Boolean enabled) {
        try {
            return Result.success(adminService.updateUserStatus(id, enabled));
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }
}