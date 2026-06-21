package com.salary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "announcement")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announce_id")
    private Integer announceId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "publisher", length = 50)
    private String publisher;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_top")
    @Builder.Default
    private Boolean isTop = false;
}
