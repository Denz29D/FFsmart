package com.example.FFsmartBackend.models;

import java.util.Date;

public class Delivery {

    private String id;
    private String itemName;
    private int quantity;
    private Date deliveryDate;
    private String deliveredBy;
    private String approvalStatus;  // New field to track approval (e.g., "Pending", "Approved", "Declined")

    public Delivery() {}

    public Delivery(String id, String itemName, int quantity, Date deliveryDate, String deliveredBy, String approvalStatus) {
        this.id = id;
        this.itemName = itemName;
        this.quantity = quantity;
        this.deliveryDate = deliveryDate;
        this.deliveredBy = deliveredBy;
        this.approvalStatus = approvalStatus;
    }

    // Getters and Setters
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

    public Date getDeliveryDate() {
        return deliveryDate;
    }
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDeliveredBy() {
        return deliveredBy;
    }
    public void setDeliveredBy(String deliveredBy) {
        this.deliveredBy = deliveredBy;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }
    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
}
