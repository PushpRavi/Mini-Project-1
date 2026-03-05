package com.zeta.service;

import com.zeta.model.entity.AuditLog;
import com.zeta.repository.fileimpl.FileAuditRepository;
import com.zeta.service.impl.AuditServiceImpl;
import com.zeta.service.interfaces.AuditService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TestAuditServiceIntegration {

    private AuditService auditService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        auditService = new AuditServiceImpl(
            new FileAuditRepository(tempDir.resolve("audit.json").toString())
        );
    }

    @Test
    void testEmpty() {
        assertEquals(0, auditService.getAllLogs().size());
    }

    @Test
    void testLogOne() {
        auditService.log("admin", "TEST", "Testing");
        assertEquals(1, auditService.getAllLogs().size());
    }

    @Test
    void testLogContent() {
        auditService.log("srinath", "LOGIN", "Logged in");
        AuditLog log = auditService.getAllLogs().get(0);
        assertEquals("srinath", log.getUsername());
    }

    @Test
    void testMultiple() {
        auditService.log("admin", "A1", "First");
        auditService.log("fm1", "A2", "Second");
        assertEquals(2, auditService.getAllLogs().size());
    }

    @Test
    void testTimestamp() {
        auditService.log("admin", "CHECK", "Test");
        AuditLog log = auditService.getAllLogs().get(0);
        assertNotNull(log.getTimestamp());
    }
}
