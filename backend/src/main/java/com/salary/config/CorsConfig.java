package com.salary.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 跨域配置
 *
 * 生产环境通过 app.cors.allowed-origins 指定允许的域名（逗号分隔）
 * 包含 localhost / 127.0.0.1 / 内网 IP / 公网域名 / HTTPS 域名
 *
 * 注意: allowCredentials=true 时不允许使用 *，必须列出明确源
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.max-age:3600}")
    private Long maxAge;

    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 解析允许的源
        List<String> origins = parseOrigins(allowedOrigins);
        config.setAllowedOrigins(origins);

        // 如果是 * 则使用 allowedOriginPatterns（搭配 credentials=false）
        if ("*".equals(allowedOrigins.trim())) {
            config.setAllowedOriginPatterns(Arrays.asList("*"));
            config.setAllowCredentials(false);
        } else {
            config.setAllowCredentials(true);
        }

        config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));

        // 暴露的响应头
        config.setExposedHeaders(Arrays.asList(
                "Authorization", "Content-Disposition", "X-Total-Count"
        ));

        config.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private List<String> parseOrigins(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Arrays.asList("*");
        }
        List<String> result = new ArrayList<>();
        for (String o : raw.split(",")) {
            String trimmed = o.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result.isEmpty() ? Arrays.asList("*") : result;
    }
}
