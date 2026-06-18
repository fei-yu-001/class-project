package com.salary.service;

import com.salary.dto.PasswordChangeRequest;
import com.salary.entity.User;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    User getProfile();
    User updateProfile(User user);
    String uploadAvatar(MultipartFile file);
    Resource getAvatarResource(String fileName);
    void changePassword(PasswordChangeRequest request);
}