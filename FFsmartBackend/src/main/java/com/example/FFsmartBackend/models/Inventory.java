package com.example.FFsmartBackend.models;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class Inventory {

    private String id;  // We'll set this manually (UUID, etc.)

    @NotBlank
    private String itemName;

    @NotNull
    private Integer quantity;

    @NotBlank
    private String type; // e.g., Vegetable, Dairy, etc.

    @NotNull
    private Date expiryDate;

    @NotNull
    private Integer thresholdQuantity; // triggers low-stock alerts
       // for storing fridge location
    @NotBlank  
    private String fridgeLocation;

    public Inventory() {}

    public Inventory(
        String id,
        String itemName,
        Integer quantity,
        String type,
        Date expiryDate,
        Integer thresholdQuantity,
        String fridgeLocation
    ) {
        this.id = id;
        this.itemName = itemName;
        this.quantity = quantity;
        this.type = type;
        this.expiryDate = expiryDate;
        this.thresholdQuantity = thresholdQuantity;
        this.fridgeLocation = fridgeLocation;
    }
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
    public String getFridgeLocation() {
        return fridgeLocation;
    }

    public void setFridgeLocation(String fridgeLocation) {
        this.fridgeLocation = fridgeLocation;
    }

    public Integer getThresholdQuantity() {
        return thresholdQuantity;
    }

    public void setThresholdQuantity(Integer thresholdQuantity) {
        this.thresholdQuantity = thresholdQuantity;
    }
}
