package com.salary.controller;

import com.salary.common.Result;
import com.salary.dto.PasswordChangeRequest;
import com.salary.entity.User;
import com.salary.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<User> getProfile() {
        return Result.success(userService.getProfile());
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody User user) {
        return Result.success(userService.updateProfile(user));
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            String avatarUrl = userService.uploadAvatar(file);
            return Result.success("上传成功", avatarUrl);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/avatar/{fileName}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String fileName) {
        Resource resource = userService.getAvatarResource(fileName);
        if (resource == null || !resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        try {
            String contentType = Files.probeContentType(Paths.get(resource.getURI()));
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        try {
            userService.changePassword(request);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}