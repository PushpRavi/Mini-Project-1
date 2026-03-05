package com.zeta.service.interfaces;

import com.zeta.model.entity.AuditLog;

import java.util.List;

public interface
AuditService {
    void log(String username, String action, String details);

    List<AuditLog> getAllLogs();
}