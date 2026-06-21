package com.salary.service;

import com.salary.entity.Announcement;

import java.util.List;

public interface AnnouncementService {
    List<Announcement> listAll();
    Announcement create(Announcement announcement, String publisher);
    void delete(Integer id);
    Announcement toggleTop(Integer id);
}
