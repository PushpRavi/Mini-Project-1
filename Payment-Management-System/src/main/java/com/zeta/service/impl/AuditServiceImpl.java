package com.zeta.service.impl;

import com.zeta.model.entity.AuditLog;
import com.zeta.repository.interfaces.AuditRepository;
import com.zeta.service.interfaces.AuditService;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class AuditServiceImpl implements AuditService {

    private static final Logger LOGGER = Logger.getLogger(AuditServiceImpl.class.getName());

    private final AuditRepository auditRepository;
    private final Random random = new Random();

    public AuditServiceImpl(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Override
    public void log(String username, String action, String details) {
        int id = 100000 + random.nextInt(900000);
        AuditLog entry = new AuditLog(id, Instant.now(), username, action, details);
        auditRepository.add(entry);
        LOGGER.info(String.format("Audit: [%s] %s - %s", username, action, details));
    }

    @Override
    public List<AuditLog> getAllLogs() {
        return auditRepository.getAll();
    }
}