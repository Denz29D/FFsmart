package com.example.FFsmartBackend.services;

import com.example.FFsmartBackend.models.Inventory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class InventoryService {

    private static final String INVENTORY_FILE = "src/main/resources/data/inventory.json";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private List<Inventory> inventoryList = new ArrayList<>();

    // Load inventory on startup
    public InventoryService() {
        loadInventoryFromFile();
    }

    private void loadInventoryFromFile() {
        try {
            byte[] jsonData = Files.readAllBytes(Paths.get(INVENTORY_FILE));
            inventoryList = objectMapper.readValue(jsonData, new TypeReference<List<Inventory>>() {});
        } catch (IOException e) {
            // If file not found or empty, start with an empty list
            inventoryList = new ArrayList<>();
        }
    }

    private void saveInventoryToFile() {
        try {
            String jsonString = objectMapper.writeValueAsString(inventoryList);
            Files.write(Paths.get(INVENTORY_FILE), jsonString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------
    // Basic CRUD
    // -------------------------------------------------------------------
    public List<Inventory> getAllItems() {
        return inventoryList;
    }

    public Optional<Inventory> findById(String id) {
        return inventoryList.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst();
    }

    public Inventory createItem(Inventory item) {
        // Generate new ID if not provided
        if (item.getId() == null || item.getId().isEmpty()) {
            item.setId(UUID.randomUUID().toString());
        }
        inventoryList.add(item);
        saveInventoryToFile();
        return item;
    }

    public boolean updateItem(String id, Inventory updatedItem) {
        Optional<Inventory> existingOpt = findById(id);
        if (existingOpt.isPresent()) {
            Inventory existing = existingOpt.get();
    
            // Update all relevant fields
            if (updatedItem.getItemName() != null) {
                existing.setItemName(updatedItem.getItemName());
            }
            if (updatedItem.getQuantity() >= 0) {
                existing.setQuantity(updatedItem.getQuantity());
            }
            if (updatedItem.getExpiryDate() != null) {
                existing.setExpiryDate(updatedItem.getExpiryDate());
            }
            if (updatedItem.getFridgeLocation() != null) {
                existing.setFridgeLocation(updatedItem.getFridgeLocation());
            }
            if (updatedItem.getType() != null) {
                existing.setType(updatedItem.getType());
            }
            if (updatedItem.getThresholdQuantity() >= 0) {
                existing.setThresholdQuantity(updatedItem.getThresholdQuantity());
            }
    
            // Save the updated item to the file
            saveInventoryToFile();
            return true;
        }
        return false;
    }
    
    

    public boolean deleteItem(String id) {
        Optional<Inventory> existingOpt = findById(id);
        if (existingOpt.isPresent()) {
            inventoryList.remove(existingOpt.get());
            saveInventoryToFile();
            return true;
        }
        return false;
    }

    // -------------------------------------------------------------------
    // Additional functionalities
    // -------------------------------------------------------------------

    /**
     * Find items whose quantity < thresholdQuantity.
     */
    public List<Inventory> getLowStockItems() {
        List<Inventory> lowStock = new ArrayList<>();
        for (Inventory item : inventoryList) {
            if (item.getQuantity() < item.getThresholdQuantity()) {
                lowStock.add(item);
            }
        }
        return lowStock;
    }

    /**
     * Find items that are expiring soon (e.g., within 7 days from now).
     * You can adjust the logic or make 'days' a parameter.
     */
    public List<Inventory> getExpiringSoon() {
        List<Inventory> expiring = new ArrayList<>();
        long now = System.currentTimeMillis();
        long sevenDaysFromNow = now + (7L * 24 * 60 * 60 * 1000); // 7 days in ms

        for (Inventory item : inventoryList) {
            Date expiry = item.getExpiryDate();
            if (expiry != null && expiry.getTime() <= sevenDaysFromNow) {
                expiring.add(item);
            }
        }
        return expiring;
    }
}
