package com.zeta.service.interfaces;

import com.zeta.model.entity.User;
import com.zeta.model.enums.Role;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> authenticate(String username, String password, Role requiredRole);

    User createFinanceManager(User adminUser, String username, String password);

    List<User> getAllUsers();
}