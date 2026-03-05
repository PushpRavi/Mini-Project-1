package com.zeta.service;

import com.zeta.model.entity.Payment;
import com.zeta.model.entity.User;
import com.zeta.model.enums.*;
import com.zeta.repository.fileimpl.FileAuditRepository;
import com.zeta.repository.fileimpl.FilePaymentRepository;
import com.zeta.repository.fileimpl.FileUserRepository;
import com.zeta.service.impl.AuditServiceImpl;
import com.zeta.service.impl.PaymentServiceImpl;
import com.zeta.service.impl.UserServiceImpl;
import com.zeta.service.interfaces.AuditService;
import com.zeta.service.interfaces.PaymentService;
import com.zeta.service.interfaces.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TestPaymentServiceIntegration {

    private UserService userService;
    private PaymentService paymentService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        AuditService auditService = new AuditServiceImpl(
            new FileAuditRepository(tempDir.resolve("audit.json").toString())
        );
        userService = new UserServiceImpl(
            new FileUserRepository(tempDir.resolve("users.json").toString()),
            auditService
        );
        paymentService = new PaymentServiceImpl(
            new FilePaymentRepository(tempDir.resolve("payments.json").toString()),
            auditService
        );
    }

    private User getAdmin() {
        return userService.authenticate("admin", "admin123", Role.ADMIN).get();
    }

    @Test
    void testAddPayment() {
        Payment p = paymentService.addPayment(getAdmin(),
            PaymentType.CREDIT, PaymentCategory.CLIENT_PAYMENT, new BigDecimal("5000"), "Inv");
        assertEquals(PaymentType.CREDIT, p.getType());
    }

    @Test
    void testDefaultPending() {
        Payment p = paymentService.addPayment(getAdmin(),
            PaymentType.DEBIT, PaymentCategory.SALARY, new BigDecimal("3000"), "Sal");
        assertEquals(PaymentStatus.PENDING, p.getStatus());
    }

    @Test
    void testUpdateStatus() {
        Payment p = paymentService.addPayment(getAdmin(),
            PaymentType.DEBIT, PaymentCategory.VENDOR_PAYMENT, new BigDecimal("2000"), "V");
        var updated = paymentService.updateStatus(getAdmin(), p.getId(), PaymentStatus.COMPLETED);
        assertEquals(PaymentStatus.COMPLETED, updated.get().getStatus());
    }

    @Test
    void testUpdateMissing() {
        var result = paymentService.updateStatus(getAdmin(), 999999, PaymentStatus.COMPLETED);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAll() {
        paymentService.addPayment(getAdmin(),
            PaymentType.CREDIT, PaymentCategory.CLIENT_PAYMENT, new BigDecimal("100"), "A");
        assertEquals(1, paymentService.getAllPayments().size());
    }

    @Test
    void testGetByUser() {
        paymentService.addPayment(getAdmin(),
            PaymentType.CREDIT, PaymentCategory.CLIENT_PAYMENT, new BigDecimal("500"), "B");
        assertEquals(1, paymentService.getPaymentsByUser("admin").size());
    }

    @Test
    void testExists() {
        Payment p = paymentService.addPayment(getAdmin(),
            PaymentType.CREDIT, PaymentCategory.CLIENT_PAYMENT, new BigDecimal("700"), "C");
        assertTrue(paymentService.paymentExists(p.getId()));
    }

    @Test
    void testNotExists() {
        assertFalse(paymentService.paymentExists(999999));
    }
}
