package com.zeta.service.impl;

import com.zeta.model.entity.Payment;
import com.zeta.model.entity.User;
import com.zeta.model.enums.PaymentCategory;
import com.zeta.model.enums.PaymentStatus;
import com.zeta.model.enums.PaymentType;
import com.zeta.repository.interfaces.PaymentRepository;
import com.zeta.service.interfaces.AuditService;
import com.zeta.service.interfaces.PaymentService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class PaymentServiceImpl implements PaymentService {

    private static final Logger LOGGER = Logger.getLogger(PaymentServiceImpl.class.getName());

    private static final AtomicLong ID_COUNTER = new AtomicLong(System.currentTimeMillis() % 900000 + 100000);

    private final PaymentRepository paymentRepository;
    private final AuditService auditService;
    private final ReentrantLock paymentLock = new ReentrantLock();

    public PaymentServiceImpl(PaymentRepository paymentRepository, AuditService auditService) {
        this.paymentRepository = paymentRepository;
        this.auditService = auditService;
    }

    private int nextId() {
        return (int) (ID_COUNTER.getAndIncrement() % 900000 + 100000);
    }

    @Override
    public Payment addPayment(User user, PaymentType type, PaymentCategory category, BigDecimal amount, String description) {
        paymentLock.lock();
        try {
            Instant now = Instant.now();
            int paymentId = nextId();
            Payment payment = new Payment(paymentId, type, category, amount, PaymentStatus.PENDING, description, now, now, user.getUsername(), user.getUsername());
            paymentRepository.add(payment);
            auditService.log(user.getUsername(), "ADD_PAYMENT", String.format("Payment %d added: %s, %s, %s", paymentId, type, category.getDisplayName(), amount));
            LOGGER.info(String.format("Payment %d added by '%s': %s, %s, %s", paymentId, user.getUsername(), type, category.getDisplayName(), amount));
            return payment;
        } finally {
            paymentLock.unlock();
        }
    }

    @Override
    public Optional<Payment> updateStatus(User user, int paymentId, PaymentStatus newStatus) {
        paymentLock.lock();
        try {
            Optional<Payment> found = paymentRepository.findById(paymentId);
            if (found.isEmpty()) {
                LOGGER.warning(String.format("Payment ID %d not found for status update.", paymentId));
                return Optional.empty();
            }
            Payment payment = found.get();
            payment.setStatus(newStatus);
            payment.setUpdatedAt(Instant.now());
            payment.setUpdatedBy(user.getUsername());
            paymentRepository.update(payment);
            auditService.log(user.getUsername(), "UPDATE_STATUS", String.format("Payment %d status changed to %s", paymentId, newStatus));
            LOGGER.info(String.format("Payment %d status updated to %s by '%s'.", paymentId, newStatus, user.getUsername()));
            return Optional.of(payment);
        } finally {
            paymentLock.unlock();
        }
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.getAll();
    }

    @Override
    public List<Payment> getPaymentsByUser(String username) {
        LOGGER.info(String.format("Fetching payments for user '%s'.", username));
        return paymentRepository.findByCreatedBy(username);
    }


    @Override
    public boolean paymentExists(int paymentId) {
        return paymentRepository.findById(paymentId).isPresent();
    }
}