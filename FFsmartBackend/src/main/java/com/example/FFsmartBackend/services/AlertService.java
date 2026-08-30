package com.example.FFsmartBackend.services;

import com.example.FFsmartBackend.models.Alert;
import com.example.FFsmartBackend.models.Inventory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AlertService {

    private static final String ALERTS_FILE = "src/main/resources/data/alerts.json";
    private static final String INVENTORY_FILE = "src/main/resources/data/inventory.json";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private List<Alert> alertList = new ArrayList<>();

    public AlertService() {
        loadAlertsFromFile();  // Ensure alerts are loaded when service starts
    }

    public void addWebSocketSession(WebSocketSession session) {
        sessions.add(session);
    }

    public void removeWebSocketSession(WebSocketSession session) {
        sessions.remove(session);
    }

    public List<Alert> getAllAlerts() {
        // Refresh alerts from file before returning to avoid stale in-memory state
        loadAlertsFromFile();
        return alertList;
    }

    public void acknowledgeAlert(String id) {
        alertList.stream()
                .filter(alert -> alert.getId().equals(id))
                .findFirst()
                .ifPresent(alert -> {
                    alert.setAcknowledged(true);
                    saveAlertsToFile();  // Persist the change after acknowledging
                });
    }

    private void loadAlertsFromFile() {
        try {
            if (!Files.exists(Paths.get(ALERTS_FILE))) {
                Files.createDirectories(Paths.get(ALERTS_FILE).getParent());
                Files.createFile(Paths.get(ALERTS_FILE));
                Files.write(Paths.get(ALERTS_FILE), "[]".getBytes());
            }
    
            byte[] jsonData = Files.readAllBytes(Paths.get(ALERTS_FILE));
            alertList = objectMapper.readValue(jsonData, objectMapper.getTypeFactory().constructCollectionType(List.class, Alert.class));
        } catch (IOException e) {
            e.printStackTrace();
            alertList = new ArrayList<>();  // Initialize an empty list if reading fails
        }
    }
    

    private void saveAlertsToFile() {
        try {
            String jsonData = objectMapper.writeValueAsString(alertList);
            Files.write(Paths.get(ALERTS_FILE), jsonData.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateExpiryAlerts() {
        List<Inventory> inventoryItems = loadInventoryFromFile();
        LocalDate today = LocalDate.now();

        for (Inventory item : inventoryItems) {
            LocalDate expiryDate = item.getExpiryDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            long daysUntilExpiry = ChronoUnit.DAYS.between(today, expiryDate);

            if (daysUntilExpiry == 5 || daysUntilExpiry == 3 || daysUntilExpiry == 0) {
                String message = generateAlertMessage(item.getItemName(), daysUntilExpiry);

                boolean alertExists = alertList.stream()
                        .anyMatch(alert -> alert.getItemId().equals(item.getId()) &&
                                alert.getMessage().equals(message) &&
                                !alert.isAcknowledged());

                if (!alertExists) {
                    Alert newAlert = new Alert(
                            UUID.randomUUID().toString(),
                            item.getId(),
                            message,
                            false,
                            new Date()
                    );

                    alertList.add(newAlert);
                    saveAlertsToFile();  // Save the alert immediately
                    sendAlertToWebSocket(newAlert);
                }
            }
        }
    }

    private void sendAlertToWebSocket(Alert alert) {
        try {
            String alertJson = objectMapper.writeValueAsString(alert);
            TextMessage alertMessage = new TextMessage(alertJson);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(alertMessage);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to send WebSocket message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Inventory> loadInventoryFromFile() {
        try {
            byte[] jsonData = Files.readAllBytes(Paths.get(INVENTORY_FILE));
            return objectMapper.readValue(jsonData, objectMapper.getTypeFactory().constructCollectionType(List.class, Inventory.class));
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String generateAlertMessage(String itemName, long daysUntilExpiry) {
        if (daysUntilExpiry == 0) {
            return "Item '" + itemName + "' is expiring today!";
        } else {
            return "Item '" + itemName + "' will expire in " + daysUntilExpiry + " day(s).";
        }
    }
}
