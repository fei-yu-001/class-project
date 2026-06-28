package com.salary.controller;

import com.salary.common.Result;
import com.salary.entity.SysConfig;
import com.salary.security.RequireRole;
import com.salary.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sys-config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @GetMapping
    @RequireRole("ADMIN")
    public Result<List<SysConfig>> getAll() {
        return Result.success(sysConfigService.getAll());
    }

    @PutMapping("/{key}")
    @RequireRole("ADMIN")
    public Result<SysConfig> update(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("configValue");
        if (value == null || value.isBlank()) {
            return Result.badRequest("配置值不能为空");
        }
        return Result.success(sysConfigService.update(key, value));
    }
}
