package com.salary.service.impl;

import com.salary.entity.Announcement;
import com.salary.repository.AnnouncementRepository;
import com.salary.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    @Override
    public List<Announcement> listAll() {
        return announcementRepository.findAllByOrderByIsTopDescCreatedAtDesc();
    }

    @Override
    @Transactional
    public Announcement create(Announcement announcement, String publisher) {
        announcement.setPublisher(publisher);
        announcement.setCreatedAt(LocalDateTime.now());
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(false);
        }
        return announcementRepository.save(announcement);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        announcementRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Announcement toggleTop(Integer id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("公告不存在"));
        announcement.setIsTop(!Boolean.TRUE.equals(announcement.getIsTop()));
        return announcementRepository.save(announcement);
    }
}
