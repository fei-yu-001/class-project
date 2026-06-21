package com.salary.config;

import com.salary.entity.User;
import com.salary.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Value("${app.security.initial-admin-password:}")
    private String initialAdminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        fixNullPasswords();

        if (userRepository.findByUsername("DDyu").isPresent()) {
            return;
        }

        boolean prod = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        String password = initialAdminPassword == null ? "" : initialAdminPassword.trim();
        if (password.isEmpty()) {
            if (prod) {
                return;
            }
            password = "zzf050731";
        }

        if (password.length() < 12 && prod) {
            throw new IllegalStateException("生产环境初始管理员密码长度不能少于 12 位");
        }

        User admin = new User();
        admin.setUsername("DDyu");
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole("ADMIN");
        admin.setEnabled(true);
        admin.setNickname("管理员");
        userRepository.save(admin);
    }

    private void fixNullPasswords() {
        String defaultPassword = passwordEncoder.encode("zzf050731");
        List<User> users = userRepository.findAll();
        boolean updated = false;
        for (User user : users) {
            if (user.getPassword() == null) {
                user.setPassword(defaultPassword);
                userRepository.save(user);
                updated = true;
            }
        }
        if (updated) {
            System.out.println("已修复密码为空的用户记录");
        }
    }
}
