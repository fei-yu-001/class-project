package com.salary.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    @JsonIgnore
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(name = "emp_id")
    private Integer empId;

    @Column(nullable = false, length = 20)
    private String role;

    private Boolean enabled;

    private static final java.util.Set<String> VALID_ROLES = java.util.Set.of("USER", "ADMIN");

    public void setRole(String role) {
        if (role != null && !VALID_ROLES.contains(role)) {
            throw new IllegalArgumentException("无效的角色: " + role + "，允许的角色: USER, ADMIN");
        }
        this.role = role;
    }

    @Column(length = 255)
    private String avatar;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (role == null) role = "USER";
        if (enabled == null) enabled = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
