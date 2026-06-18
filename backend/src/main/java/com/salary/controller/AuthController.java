package com.salary.controller;

import com.salary.common.Result;
import com.salary.dto.AuthResponse;
import com.salary.dto.LoginRequest;
import com.salary.dto.RegisterRequest;
import com.salary.security.JwtTokenProvider;
import com.salary.service.AuthService;
import com.salary.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    @Value("${app.security.registration-enabled:false}")
    private boolean registrationEnabled;

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request) {
        if (!registrationEnabled) {
            return Result.forbidden("公网环境已关闭自助注册，请联系管理员创建账号");
        }
        authService.register(request);
        return Result.success("注册成功，请登录");
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            long expiration = jwtTokenProvider.getExpirationFromToken(token);
            if (expiration > 0) {
                redisService.blacklistToken(token, expiration);
            }
        }
        return Result.success(null);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
