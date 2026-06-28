package com.salary.controller;

import com.salary.common.Result;
import com.salary.entity.AuditLog;
import com.salary.security.RequireRole;
import com.salary.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @RequireRole("ADMIN")
    public Result<Page<AuditLog>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(auditLogService.getLogs(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }
}
