package com.zeta.config;

import com.zeta.repository.fileimpl.FileAuditRepository;
import com.zeta.repository.fileimpl.FilePaymentRepository;
import com.zeta.repository.fileimpl.FileUserRepository;
import com.zeta.repository.interfaces.AuditRepository;
import com.zeta.repository.interfaces.PaymentRepository;
import com.zeta.repository.interfaces.UserRepository;
import com.zeta.service.impl.AuditServiceImpl;
import com.zeta.service.impl.PaymentServiceImpl;
import com.zeta.service.impl.UserServiceImpl;
import com.zeta.service.interfaces.AuditService;
import com.zeta.service.interfaces.PaymentService;
import com.zeta.service.interfaces.UserService;

public class ApplicationConfig {

    private static final String USERS_FILE = "data/users.json";
    private static final String PAYMENTS_FILE = "data/payments.json";
    private static final String AUDIT_LOGS_FILE = "data/audit_logs.json";

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final AuditRepository auditRepository;

    private final AuditService auditService;
    private final UserService userService;
    private final PaymentService paymentService;

    public ApplicationConfig() {

        this.userRepository = new FileUserRepository(USERS_FILE);
        this.paymentRepository = new FilePaymentRepository(PAYMENTS_FILE);
        this.auditRepository = new FileAuditRepository(AUDIT_LOGS_FILE);

        this.auditService = new AuditServiceImpl(auditRepository);
        this.userService = new UserServiceImpl(userRepository, auditService);
        this.paymentService = new PaymentServiceImpl(paymentRepository, auditService);
    }

    public UserService getUserService() {
        return userService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public AuditService getAuditService() {
        return auditService;
    }
}
