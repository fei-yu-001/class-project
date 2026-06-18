package com.salary.config;

import com.salary.entity.User;
import com.salary.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Value("${app.security.initial-super-admin-password:}")
    private String initialSuperAdminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("DDyu").isPresent()) {
            return;
        }

        boolean prod = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        String password = initialSuperAdminPassword == null ? "" : initialSuperAdminPassword.trim();
        if (password.isEmpty()) {
            if (prod) {
                return;
            }
            password = "zzf050731";
        }

        if (password.length() < 12 && prod) {
            throw new IllegalStateException("生产环境初始超级管理员密码长度不能少于 12 位");
        }

        User superAdmin = new User();
        superAdmin.setUsername("DDyu");
        superAdmin.setPassword(passwordEncoder.encode(password));
        superAdmin.setRole("SUPER_ADMIN");
        superAdmin.setEnabled(true);
        superAdmin.setNickname("超级管理员");
        userRepository.save(superAdmin);
    }
}
