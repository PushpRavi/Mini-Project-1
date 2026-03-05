package com.zeta.service;

import com.zeta.model.entity.User;
import com.zeta.model.enums.Role;
import com.zeta.repository.fileimpl.FileAuditRepository;
import com.zeta.repository.fileimpl.FileUserRepository;
import com.zeta.service.impl.AuditServiceImpl;
import com.zeta.service.impl.UserServiceImpl;
import com.zeta.service.interfaces.AuditService;
import com.zeta.service.interfaces.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TestUserServiceIntegration {

    private UserService userService;
    private AuditService auditService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        auditService = new AuditServiceImpl(
            new FileAuditRepository(tempDir.resolve("audit.json").toString())
        );
        userService = new UserServiceImpl(
            new FileUserRepository(tempDir.resolve("users.json").toString()),
            auditService
        );
    }

    @Test
    void testAdminLogin() {
        assertTrue(userService.authenticate("admin", "admin123", Role.ADMIN).isPresent());
    }

    @Test
    void testWrongPassword() {
        assertTrue(userService.authenticate("admin", "wrong", Role.ADMIN).isEmpty());
    }

    @Test
    void testNullPassword() {
        assertTrue(userService.authenticate("admin", null, Role.ADMIN).isEmpty());
    }

    @Test
    void testUnknownUser() {
        assertTrue(userService.authenticate("pushp", "pass", Role.ADMIN).isEmpty());
    }

    @Test
    void testWrongRole() {
        assertTrue(userService.authenticate("admin", "admin123", Role.FINANCE_MANAGER).isEmpty());
    }

    @Test
    void testCreateManager() {
        User admin = userService.authenticate("admin", "admin123", Role.ADMIN).get();
        User fm = userService.createFinanceManager(admin, "srinath", "pass1");
        assertEquals(Role.FINANCE_MANAGER, fm.getRole());
    }

    @Test
    void testManagerCanLogin() {
        User admin = userService.authenticate("admin", "admin123", Role.ADMIN).get();
        userService.createFinanceManager(admin, "srinath", "pass2");
        assertTrue(userService.authenticate("srinath", "pass2", Role.FINANCE_MANAGER).isPresent());
    }

    @Test
    void testDuplicateThrows() {
        User admin = userService.authenticate("admin", "admin123", Role.ADMIN).get();
        userService.createFinanceManager(admin, "srinath", "pass");
        assertThrows(IllegalArgumentException.class,
            () -> userService.createFinanceManager(admin, "srinath", "x"));
    }

    @Test
    void testNonAdminThrows() {
        User admin = userService.authenticate("admin", "admin123", Role.ADMIN).get();
        User fm = userService.createFinanceManager(admin, "sri", "pass");
        assertThrows(IllegalArgumentException.class,
            () -> userService.createFinanceManager(fm, "pushpp", "x"));
    }

    @Test
    void testGetAllUsers() {
        User admin = userService.authenticate("admin", "admin123", Role.ADMIN).get();
        userService.createFinanceManager(admin, "u1", "p1");
        assertEquals(2, userService.getAllUsers().size());
    }
}

