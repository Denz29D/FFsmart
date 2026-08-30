package com.example.FFsmartBackend.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class Alert {
    private String id;
    private String itemId;
    private String message;
    private boolean acknowledged;
    private Date createdDate;

    // No-arg constructor required by Jackson
    public Alert() {
    }

    // Constructor for ease of instantiation
    @JsonCreator
    public Alert(
        @JsonProperty("id") String id,
        @JsonProperty("itemId") String itemId,
        @JsonProperty("message") String message,
        @JsonProperty("acknowledged") boolean acknowledged,
        @JsonProperty("createdDate") Date createdDate
    ) {
        this.id = id;
        this.itemId = itemId;
        this.message = message;
        this.acknowledged = acknowledged;
        this.createdDate = createdDate;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
