package com.zeta.repository.fileimpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zeta.model.entity.AuditLog;
import com.zeta.repository.interfaces.AuditRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileAuditRepository implements AuditRepository {

    private static final Logger LOGGER = Logger.getLogger(FileAuditRepository.class.getName());

    private final List<AuditLog> logs = new ArrayList<>();
    private final File file;
    private final ObjectMapper mapper;

    public FileAuditRepository(String filePath) {
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
                List<AuditLog> loaded = mapper.readValue(file, new TypeReference<List<AuditLog>>() {
                });
                logs.addAll(loaded);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load audit logs: {0}", e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            mapper.writeValue(file, logs);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not save audit logs: {0}", e.getMessage());
        }
    }

    @Override
    public synchronized List<AuditLog> getAll() {
        return new ArrayList<>(logs);
    }

    @Override
    public synchronized void add(AuditLog log) {
        logs.add(log);
        saveToFile();
    }

    @Override
    public synchronized int size() {
        return logs.size();
    }
}