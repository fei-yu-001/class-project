package com.salary.controller;

import com.salary.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 公开健康检查端点 - 无需认证
 * 用于 Cloudflare Tunnel 健康探测、启动脚本就绪检测
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 数据库连通性
        try (Connection conn = dataSource.getConnection()) {
            status.put("database", conn.isValid(2) ? "UP" : "DOWN");
        } catch (Exception e) {
            status.put("database", "DOWN: " + e.getMessage());
        }

        // Redis 连通性
        try {
            String pong = redisConnectionFactory.getConnection().ping();
            status.put("redis", "UP".equalsIgnoreCase(pong) ? "UP" : "DOWN");
        } catch (Exception e) {
            status.put("redis", "DOWN: " + e.getMessage());
        }

        return Result.success(status);
    }
}
