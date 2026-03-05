package com.zeta.service.impl;

import com.zeta.model.entity.User;
import com.zeta.model.enums.Role;
import com.zeta.repository.interfaces.UserRepository;
import com.zeta.service.interfaces.AuditService;
import com.zeta.service.interfaces.UserService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private final UserRepository userRepository;
    private final AuditService auditService;
    private final Random random = new Random();

    public UserServiceImpl(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
        ensureDefaultAdmin();
    }

    private int nextId() {
        return 100000 + random.nextInt(900000);
    }

    private void ensureDefaultAdmin() {
        if (userRepository.size() > 0) {
            return;
        }
        User admin = new User(nextId(), DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD, Role.ADMIN, Instant.now());
        userRepository.add(admin);
        auditService.log("system", "CREATE_DEFAULT_ADMIN", "Default admin created");
        LOGGER.info("Default admin user created.");
    }

    @Override
    public Optional<User> authenticate(String username, String password, Role requiredRole) {
        Optional<User> found = userRepository.findByUsername(username);
        if (found.isEmpty()) {
            LOGGER.warning(String.format("Authentication failed: user '%s' not found.", username));
            return Optional.empty();
        }
        User user = found.get();
        if (user.getRole() != requiredRole) {
            LOGGER.warning(String.format("Authentication failed: role mismatch for user '%s'.", username));
            return Optional.empty();
        }
        if (password == null || !password.equals(user.getPassword())) {
            LOGGER.warning(String.format("Authentication failed: invalid password for user '%s'.", username));
            return Optional.empty();
        }
        LOGGER.info(String.format("User '%s' authenticated successfully.", username));
        return Optional.of(user);
    }

    @Override
    public User createFinanceManager(User adminUser, String username, String password) {
        if (adminUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only admin can create finance manager");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        User manager = new User(nextId(), username, password, Role.FINANCE_MANAGER, Instant.now());
        userRepository.add(manager);
        auditService.log(adminUser.getUsername(), "CREATE_FINANCE_MANAGER", String.format("Finance Manager '%s' created by %s", username, adminUser.getUsername()));
        LOGGER.info(String.format("Finance Manager '%s' created by '%s'.", username, adminUser.getUsername()));
        return manager;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.getAll();
    }
}