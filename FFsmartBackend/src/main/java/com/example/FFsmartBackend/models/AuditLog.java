package com.example.FFsmartBackend.models;

import java.util.Date;

/**
 * Represents a record of an action performed in the system.
 */
public class AuditLog {

    private String id;         // Unique identifier
    private String userId;     // The ID of the user who performed the action
    private String action;     // A short description of what happened, e.g. "Created new user"
    private Date timestamp;    // When it happened
    private String details;    // Additional details if needed

    public AuditLog() {
    }

    public AuditLog(String id, String userId, String action, Date timestamp, String details) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.timestamp = timestamp;
        this.details = details;
    }

    // Getters and setters...
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

    public String getDetails() {
        return details;
    }
    public void setDetails(String details) {
        this.details = details;
    }
}
