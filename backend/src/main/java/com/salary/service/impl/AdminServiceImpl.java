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
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User updateUserRole(Integer id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if ("ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("无法修改管理员自身角色");
        }

        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUserStatus(Integer id, Boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if ("ADMIN".equals(user.getRole()) && !enabled) {
            throw new IllegalArgumentException("无法禁用管理员自身");
        }

        user.setEnabled(enabled);
        return userRepository.save(user);
    }
}