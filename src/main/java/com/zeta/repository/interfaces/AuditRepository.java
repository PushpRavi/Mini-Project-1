package com.zeta.repository.interfaces;

import com.zeta.model.entity.AuditLog;

import java.util.List;

public interface AuditRepository {
    List<AuditLog> getAll();

    void add(AuditLog log);

    int size();
}