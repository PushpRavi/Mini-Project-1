package com.zeta.repository.fileimpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zeta.model.entity.Payment;
import com.zeta.repository.interfaces.PaymentRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FilePaymentRepository implements PaymentRepository {

    private static final Logger LOGGER = Logger.getLogger(FilePaymentRepository.class.getName());

    private final List<Payment> payments = Collections.synchronizedList(new ArrayList<>());
    private final File file;
    private final ObjectMapper mapper;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public FilePaymentRepository(String filePath) {
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
                List<Payment> loaded = mapper.readValue(file, new TypeReference<List<Payment>>() {
                });
                payments.addAll(loaded);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load payments: {0}", e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            mapper.writeValue(file, payments);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not save payments: {0}", e.getMessage());
        }
    }

    @Override
    public List<Payment> getAll() {
        rwLock.readLock().lock();
        try {
            return new ArrayList<>(payments);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public List<Payment> findByCreatedBy(String username) {
        rwLock.readLock().lock();
        try {
            return payments.stream()
                    .filter(p -> p.getCreatedBy().equals(username))
                    .toList();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public Optional<Payment> findById(int id) {
        rwLock.readLock().lock();
        try {
            return payments.stream()
                    .filter(p -> p.getId() == id)
                    .findFirst();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void add(Payment payment) {
        rwLock.writeLock().lock();
        try {
            payments.add(payment);
            saveToFile();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void update(Payment updated) {
        rwLock.writeLock().lock();
        try {
            for (int i = 0; i < payments.size(); i++) {
                if (payments.get(i).getId() == updated.getId()) {
                    payments.set(i, updated);
                    saveToFile();
                    return;
                }
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        rwLock.readLock().lock();
        try {
            return payments.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }
}