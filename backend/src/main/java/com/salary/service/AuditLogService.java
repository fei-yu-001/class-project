package com.salary.service;

import com.salary.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    void log(Integer userId, String username, String action, String entityType, Integer entityId, String detail);
    Page<AuditLog> getLogs(Pageable pageable);
}
