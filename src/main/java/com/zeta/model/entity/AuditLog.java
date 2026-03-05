package com.zeta.model.entity;

import java.time.Instant;

public class AuditLog {
    private int id;
    private Instant timestamp;
    private String username;
    private String action;
    private String details;

    public AuditLog() {
    }

    public AuditLog(int id, Instant timestamp, String username, String action, String details) {
        this.id = id;
        this.timestamp = timestamp;
        this.username = username;
        this.action = action;
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

}