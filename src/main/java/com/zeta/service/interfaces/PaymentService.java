package com.zeta.service.interfaces;

import com.zeta.model.entity.Payment;
import com.zeta.model.entity.User;
import com.zeta.model.enums.PaymentCategory;
import com.zeta.model.enums.PaymentStatus;
import com.zeta.model.enums.PaymentType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentService {
    Payment addPayment(User user, PaymentType type, PaymentCategory category, BigDecimal amount, String description);

    Optional<Payment> updateStatus(User user, int paymentId, PaymentStatus newStatus);

    List<Payment> getAllPayments();

    List<Payment> getPaymentsByUser(String username);

    boolean paymentExists(int paymentId);
}