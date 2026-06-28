package com.salary.security;

import com.salary.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salary.service.RedisService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    // 通用 API: 每分钟 120 次
    private static final int API_MAX_REQUESTS = 120;
    private static final int API_WINDOW_SECONDS = 60;

    // 登录接口: 每分钟 10 次
    private static final int LOGIN_MAX_REQUESTS = 10;
    private static final int LOGIN_WINDOW_SECONDS = 60;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String clientKey = getClientIp(httpRequest);

        // 登录接口使用更严格的限制
        if (uri.equals("/api/auth/login")) {
            if (redisService.isRateLimited("login:" + clientKey, LOGIN_MAX_REQUESTS, LOGIN_WINDOW_SECONDS)) {
                sendRateLimitError(httpResponse, "登录请求过于频繁，请稍后再试");
                return;
            }
        }
        // 所有 API 接口通用限流
        else if (uri.startsWith("/api/")) {
            if (redisService.isRateLimited("api:" + clientKey, API_MAX_REQUESTS, API_WINDOW_SECONDS)) {
                sendRateLimitError(httpResponse, "请求过于频繁，请稍后再试");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            ip = ip.split(",")[0].trim();
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private void sendRateLimitError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Result<?> result = Result.error(429, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
