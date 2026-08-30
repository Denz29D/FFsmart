package com.example.FFsmartBackend.models;

import java.util.Date;

/**
 * Represents a reorder request for low-stock items.
 */
public class Order {

    private String id;
    private String itemName;
    private int quantity;
    private String status;  // e.g. "pending", "approved", "rejected"
    private Date createdAt;

    public Order() {
    }

    public Order(String id, String itemName, int quantity, String status, Date createdAt) {
        this.id = id;
        this.itemName = itemName;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
