package com.salary.service.impl;

import com.salary.dto.AuthResponse;
import com.salary.dto.LoginRequest;
import com.salary.dto.RegisterRequest;
import com.salary.entity.User;
import com.salary.repository.EmployeeRepository;
import com.salary.repository.UserRepository;
import com.salary.security.JwtTokenProvider;
import com.salary.service.AuthService;
import com.salary.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    @Override
    public AuthResponse login(LoginRequest request) {
        String username = request.getUsername();

        // Check login rate limiting
        if (redisService.isLoginBlocked(username)) {
            long remaining = redisService.getLoginLockoutRemaining(username);
            throw new IllegalArgumentException("登录尝试次数过多，请" + remaining + "秒后再试");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    redisService.recordLoginAttempt(username);
                    return new IllegalArgumentException("用户名或密码错误");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            redisService.recordLoginAttempt(username);
            throw new IllegalArgumentException("用户名或密码错误");
        }

        if (!user.getEnabled()) {
            throw new IllegalArgumentException("账号已被禁用");
        }

        // Clear login attempts on success
        redisService.clearLoginAttempts(username);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public boolean register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        if (request.getEmpId() != null && !employeeRepository.existsById(request.getEmpId())) {
            throw new IllegalArgumentException("员工不存在");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .empId(request.getEmpId())
                .role("USER")
                .enabled(true)
                .build();

        userRepository.save(user);

        return true;
    }
}
