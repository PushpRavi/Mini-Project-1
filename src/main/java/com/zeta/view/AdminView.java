package com.zeta.view;

import com.zeta.model.entity.AuditLog;
import com.zeta.model.entity.Payment;
import com.zeta.model.entity.User;
import com.zeta.model.enums.PaymentStatus;
import com.zeta.service.interfaces.AuditService;
import com.zeta.service.interfaces.PaymentService;
import com.zeta.service.interfaces.UserService;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Logger;

public class AdminView {

    private static final Logger LOGGER = Logger.getLogger(AdminView.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());
    private static final String SEPARATOR = "========================================";

    private final UserService userService;
    private final PaymentService paymentService;
    private final AuditService auditService;
    private final Scanner scanner;

    public AdminView(UserService userService, PaymentService paymentService, AuditService auditService, Scanner scanner) {
        this.userService = userService;
        this.paymentService = paymentService;
        this.auditService = auditService;
        this.scanner = scanner;
    }

    public void showMenu(User user) {
        while (true) {
            System.out.printf("%n===== ADMIN MENU [%s] =====%n", user.getUsername());
            System.out.println("1. View Payments");
            System.out.println("2. Update Payment Status");
            System.out.println("3. Create Finance Manager");
            System.out.println("4. View Audit Logs");
            System.out.println("5. Logout");
            System.out.println("6. Exit");

            int choice = InputHelper.readInt(scanner, "Choose option: ", 1, 6);
            switch (choice) {
                case 1 -> handleViewPayments();
                case 2 -> handleUpdateStatus(user);
                case 3 -> handleCreateFinanceManager(user);
                case 4 -> handleViewAuditLogs();
                case 5 -> {
                    System.out.printf("Thank you, %s! Visit again.%n", user.getUsername());
                    LOGGER.info(String.format("Admin '%s' logged out.", user.getUsername()));
                    return;
                }
                case 6 -> {
                    System.out.printf("Thank you, %s! Visit again.%n", user.getUsername());
                    LOGGER.info(String.format("Admin '%s' exited the application.", user.getUsername()));
                    System.exit(0);
                }
            }
        }
    }

    private void handleViewPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        if (isEmptyList(payments, "No payments found.")) {
            return;
        }
        printPaymentList(payments);
    }

    private void handleUpdateStatus(User user) {
        List<Payment> payments = paymentService.getAllPayments();
        if (isEmptyList(payments, "No payments available.")) {
            return;
        }
        printPaymentList(payments);

        int paymentId = InputHelper.readInt(scanner, "Enter Payment ID: ", 100000, 999999);

        if (!paymentService.paymentExists(paymentId)) {
            System.out.println("Error: Payment ID " + paymentId + " not found. Please enter a valid payment ID.");
            return;
        }

        System.out.println("\nStatus:");
        PaymentStatus[] statuses = PaymentStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, statuses[i]);
        }
        int ch = InputHelper.readInt(scanner, "Choose status: ", 1, statuses.length);
        PaymentStatus newStatus = statuses[ch - 1];

        Optional<Payment> updated = paymentService.updateStatus(user, paymentId, newStatus);
        if (updated.isEmpty()) {
            System.out.println("Error: Payment ID not found.");
        } else {
            System.out.println("Payment updated successfully.");
        }
    }

    private void handleCreateFinanceManager(User adminUser) {
        String username = InputHelper.readString(scanner, "New username: ");
        String password = InputHelper.readString(scanner, "New password: ");
        try {
            User fm = userService.createFinanceManager(adminUser, username, password);
            System.out.printf("Finance manager created: %s%n", fm.getUsername());
        } catch (IllegalArgumentException e) {
            System.out.printf("Error: %s%n", e.getMessage());
        }
    }

    private void handleViewAuditLogs() {
        List<AuditLog> logs = auditService.getAllLogs();
        if (isEmptyList(logs, "No audit logs available.")) {
            return;
        }
        System.out.println("\n===== AUDIT LOGS =====");
        System.out.printf("%-20s | %-10s | %-21s | %s%n", "Timestamp", "User", "Action", "Details");
        System.out.println(SEPARATOR + SEPARATOR);
        for (AuditLog log : logs) {
            String time = FORMATTER.format(log.getTimestamp());
            System.out.printf("%-20s | %-10s | %-21s | %s%n",
                    time, log.getUsername(), log.getAction(), log.getDetails());
        }
    }

    private void printPaymentList(List<Payment> payments) {
        System.out.println("\n===== PAYMENTS =====");
        System.out.printf("%-8s | %-7s | %-16s | %-10s | %-10s | %-20s | %-10s%n",
                "ID", "Type", "Category", "Amount", "Status", "Created At", "Created By");
        System.out.println(SEPARATOR + SEPARATOR);
        for (Payment p : payments) {
            String time = FORMATTER.format(p.getCreatedAt());
            System.out.printf("%-8d | %-7s | %-16s | %-10s | %-10s | %-20s | %-10s%n",
                    p.getId(), p.getType(), p.getCategory().getDisplayName(),
                    p.getAmount(), p.getStatus(), time, p.getCreatedBy());
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