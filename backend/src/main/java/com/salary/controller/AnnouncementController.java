package com.salary.controller;

import com.salary.common.Result;
import com.salary.entity.Announcement;
import com.salary.security.RequireRole;
import com.salary.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public Result<List<Announcement>> listAll() {
        return Result.success(announcementService.listAll());
    }

    @PostMapping
    @RequireRole("ADMIN")
    public Result<Announcement> create(@RequestBody Announcement announcement, HttpServletRequest request) {
        String publisher = request.getRemoteUser();
        if (publisher == null || publisher.isBlank()) {
            publisher = "管理员";
        }
        return Result.success(announcementService.create(announcement, publisher));
    }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<Void> delete(@PathVariable Integer id) {
        announcementService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/top")
    @RequireRole("ADMIN")
    public Result<Announcement> toggleTop(@PathVariable Integer id) {
        return Result.success(announcementService.toggleTop(id));
    }
}
