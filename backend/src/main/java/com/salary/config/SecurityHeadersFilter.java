package com.salary.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 安全响应头过滤器
 * 为所有响应添加安全相关的 HTTP 头
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 防止点击劫持
        httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
        // 防止 MIME 类型嗅探
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        // 浏览器 XSS 防护
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        // Referrer 策略
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        // 限制浏览器特性
        httpResponse.setHeader("Permissions-Policy",
                "geolocation=(), microphone=(), camera=(), payment=()");

        // 仅 HTTPS 时启用 HSTS（生产环境通过反向代理添加更合适）
        String forwardedProto = ((jakarta.servlet.http.HttpServletRequest) request).getHeader("X-Forwarded-Proto");
        if ("https".equalsIgnoreCase(forwardedProto)) {
            httpResponse.setHeader("Strict-Transport-Security",
                    "max-age=31536000; includeSubDomains");
        }

        chain.doFilter(request, response);
    }
}
