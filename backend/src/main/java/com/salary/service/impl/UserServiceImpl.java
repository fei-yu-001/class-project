package com.salary.service.impl;

import com.salary.dto.PasswordChangeRequest;
import com.salary.entity.User;
import com.salary.repository.UserRepository;
import com.salary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String UPLOAD_DIR = "uploads/avatars/";
    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024L;
    private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
            MediaType.IMAGE_JPEG_VALUE, ".jpg",
            MediaType.IMAGE_PNG_VALUE, ".png",
            MediaType.IMAGE_GIF_VALUE, ".gif",
            "image/webp", ".webp"
    );

    private Integer getCurrentUserId() {
        return (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private User getCurrentUser() {
        return userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    @Override
    public User getProfile() {
        return getCurrentUser();
    }

    @Override
    public User updateProfile(User user) {
        User existing = getCurrentUser();
        if (user.getNickname() != null) {
            existing.setNickname(user.getNickname());
        }
        if (user.getUsername() != null) {
            existing.setUsername(user.getUsername());
        }
        return userRepository.save(existing);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        Integer userId = getCurrentUserId();

        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException("头像大小不能超过 2MB");
        }

        String contentType = file.getContentType();
        String ext = ALLOWED_IMAGE_TYPES.get(contentType);
        if (ext == null) {
            throw new IllegalArgumentException("仅支持 JPG、PNG、GIF、WEBP 图片");
        }

        String originalName = file.getOriginalFilename();
        if (originalName != null) {
            String lowerName = originalName.toLowerCase(Locale.ROOT);
            boolean extensionAllowed = lowerName.endsWith(".jpg")
                    || lowerName.endsWith(".jpeg")
                    || lowerName.endsWith(".png")
                    || lowerName.endsWith(".gif")
                    || lowerName.endsWith(".webp");
            if (!extensionAllowed) {
                throw new IllegalArgumentException("图片扩展名不合法");
            }
        }

        String fileName = UUID.randomUUID() + ext;
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName).normalize();
            if (!filePath.startsWith(uploadPath)) {
                throw new IllegalArgumentException("文件路径不合法");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
            String avatarUrl = "/api/user/avatar/" + fileName;
            user.setAvatar(avatarUrl);
            userRepository.save(user);

            return avatarUrl;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("上传失败: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
        }
    }

    @Override
    public Resource getAvatarResource(String fileName) {
        if (!fileName.matches("[0-9a-fA-F\\-]{36}\\.(jpg|png|gif|webp)")) {
            return null;
        }
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(fileName).normalize();
            if (!filePath.startsWith(uploadPath) || !Files.exists(filePath)) {
                return null;
            }
            return new UrlResource(filePath.toUri());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void changePassword(PasswordChangeRequest request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}