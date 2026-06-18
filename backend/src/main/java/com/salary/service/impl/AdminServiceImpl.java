package com.salary.service.impl;

import com.salary.entity.User;
import com.salary.repository.UserRepository;
import com.salary.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    public List<User> listUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    @Transactional
    public User updateUserRole(Integer id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if ("SUPER_ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("无法修改超级管理员的角色");
        }

        user.setRole(role);
        User saved = userRepository.save(user);
        saved.setPassword(null);
        return saved;
    }

    @Override
    @Transactional
    public User updateUserStatus(Integer id, Boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if ("SUPER_ADMIN".equals(user.getRole()) && !enabled) {
            throw new IllegalArgumentException("无法禁用超级管理员");
        }

        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        saved.setPassword(null);
        return saved;
    }
}