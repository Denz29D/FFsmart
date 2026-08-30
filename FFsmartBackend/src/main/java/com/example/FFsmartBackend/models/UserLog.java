package com.example.FFsmartBackend.models;


import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserLog {

    @NotBlank
    private String id;

    @NotBlank
    private String userId; // Reference to the User model

    @NotBlank
    private String action; // e.g., Added, Removed, etc.

    @NotNull
    private Date timestamp;

    @NotBlank
    private String description;

    // Constructor, Getters, and Setters
    public UserLog() {}

    public UserLog(String userId, String action, Date timestamp, String description) {
        this.userId = userId;
        this.action = action;
        this.timestamp = timestamp;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

