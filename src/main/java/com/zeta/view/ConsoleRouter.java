package com.zeta.view;

import com.zeta.model.entity.User;
import com.zeta.model.enums.Role;
import com.zeta.service.interfaces.AuditService;
import com.zeta.service.interfaces.PaymentService;
import com.zeta.service.interfaces.UserService;

import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Logger;

public class ConsoleRouter {

    private static final Logger LOGGER = Logger.getLogger(ConsoleRouter.class.getName());

    private final UserService userService;
    private final PaymentService paymentService;
    private final AuditService auditService;
    private final Scanner scanner;

    public ConsoleRouter(UserService userService, PaymentService paymentService, AuditService auditService) {
        this.userService = userService;
        this.paymentService = paymentService;
        this.auditService = auditService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("===== Welcome to Payments Management System =====");
        while (true) {
            System.out.println("\nLogin as:");
            System.out.println("1. Admin");
            System.out.println("2. Finance Manager");
            System.out.println("3. Exit");

            int choice = InputHelper.readInt(scanner, "Choose option: ", 1, 3);
            if (choice == 3) {
                System.out.println("Thank you! Visit again.");
                LOGGER.info("Application exited by user.");
                scanner.close();
                return;
            }
            Role role = (choice == 1) ? Role.ADMIN : Role.FINANCE_MANAGER;
            String username = InputHelper.readString(scanner, "Username: ");
            String password = InputHelper.readString(scanner, "Password: ");

            Optional<User> userOpt = userService.authenticate(username, password, role);
            if (userOpt.isEmpty()) {
                System.out.println("Login failed. Please try again.");
                LOGGER.warning(String.format("Failed login attempt for user '%s' as %s.", username, role));
                continue;
            }
            User user = userOpt.get();
            System.out.printf("%nWelcome, %s! You are logged in as %s.%n", user.getUsername(), role);
            auditService.log(user.getUsername(), "LOGIN", String.format("User '%s' logged in as %s", user.getUsername(), role));

            if (role == Role.ADMIN) {
                new AdminView(userService, paymentService, auditService, scanner).showMenu(user);
            } else {
                new FinanceManagerView(paymentService, auditService, scanner).showMenu(user);
            }

            auditService.log(user.getUsername(), "LOGOUT", String.format("User '%s' logged out", user.getUsername()));
            LOGGER.info(String.format("User '%s' logged out.", user.getUsername()));
        }
    }
}