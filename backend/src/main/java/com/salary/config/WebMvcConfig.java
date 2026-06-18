package com.salary.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * 前后端完全分离：后端仅提供 API，前端由 Vite dev server 或独立 Nginx 托管
 * 后端不托管任何前端静态资源
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
}