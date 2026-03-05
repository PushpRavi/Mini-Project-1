package com.zeta.view;

import com.zeta.model.entity.Payment;
import com.zeta.model.entity.User;
import com.zeta.model.enums.PaymentCategory;
import com.zeta.model.enums.PaymentType;
import com.zeta.service.interfaces.AuditService;
import com.zeta.service.interfaces.PaymentService;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class FinanceManagerView {

    private static final Logger LOGGER = Logger.getLogger(FinanceManagerView.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final String SEPARATOR = "========================================";

    private final PaymentService paymentService;
    private final AuditService auditService;
    private final Scanner scanner;

    public FinanceManagerView(PaymentService paymentService, AuditService auditService, Scanner scanner) {
        this.paymentService = paymentService;
        this.auditService = auditService;
        this.scanner = scanner;
    }

    public void showMenu(User user) {
        while (true) {
            System.out.printf("%n===== FINANCE MANAGER MENU [%s] =====%n", user.getUsername());
            System.out.println("1. Add Payment");
            System.out.println("2. View Payments");
            System.out.println("3. Logout");
            System.out.println("4. Exit");

            int choice = InputHelper.readInt(scanner, "Choose option: ", 1, 4);
            switch (choice) {
                case 1 -> handleAddPayment(user);
                case 2 -> handleViewPayments(user);
                case 3 -> {
                    System.out.printf("Thank you, %s! Visit again.%n", user.getUsername());
                    LOGGER.info(String.format("Finance Manager '%s' logged out.", user.getUsername()));
                    return;
                }
                case 4 -> {
                    System.out.printf("Thank you, %s! Visit again.%n", user.getUsername());
                    LOGGER.info(String.format("Finance Manager '%s' exited the application.", user.getUsername()));
                    System.exit(0);
                }
            }
        }
    }

    private void handleAddPayment(User user) {
        System.out.println("\nPayment Type:");
        System.out.println("1. Credit");
        System.out.println("2. Debit");
        int typeChoice = InputHelper.readInt(scanner, "Choose option: ", 1, 2);
        PaymentType type = typeChoice == 1 ? PaymentType.CREDIT : PaymentType.DEBIT;

        PaymentCategory category = selectCategory(type);

        BigDecimal amount = InputHelper.readAmount(scanner, "Amount: ");
        String description = InputHelper.readString(scanner, "Description: ");

        Payment payment = paymentService.addPayment(user, type, category, amount, description);
        System.out.printf("Payment added with ID: %d%n", payment.getId());
    }

    private PaymentCategory selectCategory(PaymentType type) {
        if (type == PaymentType.CREDIT) {
            System.out.printf("%nCategory: %s (auto-selected for Credit)%n", PaymentCategory.CLIENT_PAYMENT.getDisplayName());
            return PaymentCategory.CLIENT_PAYMENT;
        } else {
            System.out.println("\nPayment Category:");
            System.out.println("1. Salary");
            System.out.println("2. Vendor Payment");
            int catChoice = InputHelper.readInt(scanner, "Choose option: ", 1, 2);
            return catChoice == 1 ? PaymentCategory.SALARY : PaymentCategory.VENDOR_PAYMENT;
        }
    }

    private void handleViewPayments(User user) {
        List<Payment> payments = paymentService.getPaymentsByUser(user.getUsername());
        if (isEmptyList(payments, "No payments found.")) {
            return;
        }
        auditService.log(user.getUsername(), "VIEW_PAYMENTS", String.format("Finance Manager '%s' viewed their payments (%d records)", user.getUsername(), payments.size()));
        LOGGER.info(String.format("Finance Manager '%s' viewed %d payment(s).", user.getUsername(), payments.size()));
        printPaymentList(payments);
    }

    private void printPaymentList(List<Payment> payments) {
        System.out.println("\n===== PAYMENTS =====");
        System.out.printf("%-8s | %-7s | %-16s | %-10s | %-10s | %-20s | %-10s%n", "ID", "Type", "Category", "Amount", "Status", "Created At", "Created By");
        System.out.println(SEPARATOR + SEPARATOR);
        for (Payment p : payments) {
            String time = FORMATTER.format(p.getCreatedAt());
            System.out.printf("%-8d | %-7s | %-16s | %-10s | %-10s | %-20s | %-10s%n", p.getId(), p.getType(), p.getCategory().getDisplayName(), p.getAmount(), p.getStatus(), time, p.getCreatedBy());
        }
    }

    private <T> boolean isEmptyList(List<T> list, String message) {
        if (list.isEmpty()) {
            System.out.println(message);
            return true;
        }
        return false;
    }
}