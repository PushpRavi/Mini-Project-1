package com.zeta.repository.fileimpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zeta.model.entity.User;
import com.zeta.repository.interfaces.UserRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUserRepository implements UserRepository {

    private static final Logger LOGGER = Logger.getLogger(FileUserRepository.class.getName());

    private final List<User> users = new ArrayList<>();
    private final File file;
    private final ObjectMapper mapper;

    public FileUserRepository(String filePath) {
        this.file = new File(filePath);
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        ensureFileExists();
        loadFromFile();
    }

    private void ensureFileExists() {
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    LOGGER.info(String.format("Created directory: %s", parentDir.getAbsolutePath()));
                }
            }
            if (!file.exists()) {
                if (file.createNewFile()) {
                    mapper.writeValue(file, new ArrayList<>());
                    LOGGER.info(String.format("Created data file: %s", file.getAbsolutePath()));
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not create data file: {0}", e.getMessage());
        }
    }

    private void loadFromFile() {
        try {
            if (file.exists() && file.length() > 2) {
                List<User> loaded = mapper.readValue(file, new TypeReference<List<User>>() {
                });
                users.addAll(loaded);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load users: {0}", e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            mapper.writeValue(file, users);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not save users: {0}", e.getMessage());
        }
    }

    @Override
    public synchronized List<User> getAll() {
        return new ArrayList<>(users);
    }

    @Override
    public synchronized Optional<User> findByUsername(String username) {
        return users.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    @Override
    public synchronized Optional<User> findById(int id) {
        return users.stream().filter(u -> u.getId() == id).findFirst();
    }

    @Override
    public synchronized void add(User user) {
        users.add(user);
        saveToFile();
    }

    @Override
    public synchronized void update(User updated) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == updated.getId()) {
                users.set(i, updated);
                saveToFile();
                return;
            }
        }
    }

    @Override
    public synchronized int size() {
        return users.size();
    }
}