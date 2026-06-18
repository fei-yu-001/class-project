package com.salary.service;

import com.salary.entity.User;
import java.util.List;

public interface AdminService {
    List<User> listUsers();
    User updateUserRole(Integer id, String role);
    User updateUserStatus(Integer id, Boolean enabled);
}