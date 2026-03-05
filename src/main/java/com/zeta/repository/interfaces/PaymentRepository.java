package com.zeta.repository.interfaces;

import com.zeta.model.entity.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    List<Payment> getAll();

    List<Payment> findByCreatedBy(String username);

    Optional<Payment> findById(int id);

    void add(Payment payment);

    void update(Payment updated);

    int size();
}