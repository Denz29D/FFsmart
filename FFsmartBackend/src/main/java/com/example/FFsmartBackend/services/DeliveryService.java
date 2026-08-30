package com.example.FFsmartBackend.services;

import com.example.FFsmartBackend.models.Delivery;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class DeliveryService {

    private static final String DELIVERIES_FILE = "src/main/resources/data/deliveries.json";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<Delivery> deliveryList = new ArrayList<>();

    public DeliveryService() {
        loadDeliveriesFromFile();
    }

    public void loadDeliveriesFromFile() {
        try {
            if (!Files.exists(Paths.get(DELIVERIES_FILE))) {
                Files.createDirectories(Paths.get(DELIVERIES_FILE).getParent());
                Files.createFile(Paths.get(DELIVERIES_FILE));
                Files.write(Paths.get(DELIVERIES_FILE), "[]".getBytes());
            }

            byte[] jsonData = Files.readAllBytes(Paths.get(DELIVERIES_FILE));
            deliveryList = objectMapper.readValue(jsonData, new TypeReference<List<Delivery>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            deliveryList = new ArrayList<>();
        }
    }

    private void saveDeliveriesToFile() {
        try {
            String json = objectMapper.writeValueAsString(deliveryList);
            Files.write(Paths.get(DELIVERIES_FILE), json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Delivery> getAllDeliveries() {
        return deliveryList;
    }

    public Optional<Delivery> findById(String id) {
        return deliveryList.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst();
    }

    public Delivery createDelivery(Delivery delivery) {
        // Generate ID if missing
        if (delivery.getId() == null || delivery.getId().isEmpty()) {
            delivery.setId(UUID.randomUUID().toString());
        }
        deliveryList.add(delivery);
        saveDeliveriesToFile();
        return delivery;
    }

    public boolean approveDelivery(String id) {
        Optional<Delivery> deliveryOpt = findById(id);
        if (deliveryOpt.isPresent()) {
            Delivery delivery = deliveryOpt.get();
            delivery.setApprovalStatus("Approved");
            saveDeliveriesToFile();
            return true;
        }
        return false;
    }

    public boolean declineDelivery(String id) {
        Optional<Delivery> deliveryOpt = findById(id);
        if (deliveryOpt.isPresent()) {
            Delivery delivery = deliveryOpt.get();
            delivery.setApprovalStatus("Declined");
            saveDeliveriesToFile();
            return true;
        }
        return false;
    }

    public boolean updateDelivery(Delivery updatedDelivery) {
        Optional<Delivery> existingOpt = findById(updatedDelivery.getId());
        if (existingOpt.isPresent()) {
            Delivery existingDelivery = existingOpt.get();

            existingDelivery.setItemName(updatedDelivery.getItemName());
            existingDelivery.setQuantity(updatedDelivery.getQuantity());
            existingDelivery.setDeliveryDate(updatedDelivery.getDeliveryDate());
            existingDelivery.setDeliveredBy(updatedDelivery.getDeliveredBy());
            existingDelivery.setApprovalStatus(updatedDelivery.getApprovalStatus());

            saveDeliveriesToFile();
            return true;
        }
        return false;
    }
}
