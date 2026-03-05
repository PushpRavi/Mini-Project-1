package com.zeta.service;

import com.zeta.model.entity.Payment;
import com.zeta.model.entity.User;
import com.zeta.model.enums.*;
import com.zeta.repository.interfaces.PaymentRepository;
import com.zeta.service.impl.PaymentServiceImpl;
import com.zeta.service.interfaces.AuditService;
import com.zeta.service.interfaces.PaymentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestPaymentServiceUnit {

    private PaymentRepository repo;
    private AuditService auditService;
    private PaymentService service;
    private User admin;
    private User fm;

    @BeforeEach
    void setUp() {
        repo = mock(PaymentRepository.class);
        auditService = mock(AuditService.class);
        service = new PaymentServiceImpl(repo, auditService);
        admin = new User(1, "admin", "pass", Role.ADMIN, Instant.now());
        fm = new User(2, "fm1", "pass", Role.FINANCE_MANAGER, Instant.now());
    }

    @Test
    void testAddCredit() {
        Payment p = service.addPayment(admin,
            PaymentType.CREDIT, PaymentCategory.CLIENT_PAYMENT, new BigDecimal("5000"), "Inv");
        assertEquals(PaymentType.CREDIT, p.getType());
    }

    @Test
    void testAddDebit() {
        Payment p = service.addPayment(admin,
            PaymentType.DEBIT, PaymentCategory.SALARY, new BigDecimal("3000"), "Sal");
        assertEquals(PaymentCategory.SALARY, p.getCategory());
    }

    @Test
    void testAddPending() {
        Payment p = service.addPayment(admin,
            PaymentType.CREDIT, PaymentCategory.VENDOR_PAYMENT, new BigDecimal("100"), "T");
        assertEquals(PaymentStatus.PENDING, p.getStatus());
    }

    @Test
    void testAddCreatedBy() {
        Payment p = service.addPayment(fm,
            PaymentType.DEBIT, PaymentCategory.VENDOR_PAYMENT, new BigDecimal("400"), "S");
        assertEquals("fm1", p.getCreatedBy());
    }

    @Test
    void testAddSavesToRepo() {
        service.addPayment(admin,
            PaymentType.CREDIT, PaymentCategory.SALARY, new BigDecimal("1200"), "P");
        verify(repo).add(any(Payment.class));
    }

    @Test
    void testAddLogsAudit() {
        service.addPayment(admin,
            PaymentType.CREDIT, PaymentCategory.SALARY, new BigDecimal("800"), "B");
        verify(auditService).log(eq("admin"), eq("ADD_PAYMENT"), anyString());
    }

    @Test
    void testUpdateCompleted() {
        Payment p = makePayment(101, PaymentStatus.PENDING);
        when(repo.findById(101)).thenReturn(Optional.of(p));
        var r = service.updateStatus(admin, 101, PaymentStatus.COMPLETED);
        assertEquals(PaymentStatus.COMPLETED, r.get().getStatus());
    }

    @Test
    void testUpdateProcessing() {
        Payment p = makePayment(102, PaymentStatus.PENDING);
        when(repo.findById(102)).thenReturn(Optional.of(p));
        service.updateStatus(fm, 102, PaymentStatus.PROCESSING);
        assertEquals(PaymentStatus.PROCESSING, p.getStatus());
    }

    @Test
    void testUpdateRejected() {
        Payment p = makePayment(103, PaymentStatus.PENDING);
        when(repo.findById(103)).thenReturn(Optional.of(p));
        service.updateStatus(admin, 103, PaymentStatus.REJECTED);
        assertEquals(PaymentStatus.REJECTED, p.getStatus());
    }

    @Test
    void testUpdateNotFound() {
        when(repo.findById(999)).thenReturn(Optional.empty());
        assertTrue(service.updateStatus(admin, 999, PaymentStatus.COMPLETED).isEmpty());
    }

    @Test
    void testGetAll() {
        when(repo.getAll()).thenReturn(List.of(makePayment(1, PaymentStatus.PENDING)));
        assertEquals(1, service.getAllPayments().size());
    }

    @Test
    void testGetByUser() {
        when(repo.findByCreatedBy("fm1")).thenReturn(List.of(makePayment(10, PaymentStatus.PENDING)));
        assertEquals(1, service.getPaymentsByUser("fm1").size());
    }

    @Test
    void testExists() {
        when(repo.findById(200)).thenReturn(Optional.of(makePayment(200, PaymentStatus.PENDING)));
        assertTrue(service.paymentExists(200));
    }

    @Test
    void testNotExists() {
        when(repo.findById(404)).thenReturn(Optional.empty());
        assertFalse(service.paymentExists(404));
    }

    private Payment makePayment(int id, PaymentStatus status) {
        return new Payment(id, PaymentType.CREDIT, PaymentCategory.SALARY,
            new BigDecimal("1000"), status, "Test",
            Instant.now(), Instant.now(), "admin", "admin");
    }
}
