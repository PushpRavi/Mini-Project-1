package com.zeta.repository.interfaces;

import com.zeta.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> getAll();

    Optional<User> findByUsername(String username);

    Optional<User> findById(int id);

    void add(User user);

    void update(User updated);

    int size();
}