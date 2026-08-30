package com.example.FFsmartBackend.services;

import com.example.FFsmartBackend.models.Order;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class OrderService {

    private static final String ORDERS_FILE = "src/main/resources/data/orders.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<Order> orderList = new ArrayList<>();

    public OrderService() {
        loadOrdersFromFile();
    }

    public void loadOrdersFromFile() {
        try {
            if (!Files.exists(Paths.get(ORDERS_FILE))) {
                Files.createDirectories(Paths.get(ORDERS_FILE).getParent());
                Files.createFile(Paths.get(ORDERS_FILE));
                Files.write(Paths.get(ORDERS_FILE), "[]".getBytes());
            }
            byte[] jsonData = Files.readAllBytes(Paths.get(ORDERS_FILE));
            orderList = objectMapper.readValue(jsonData, new TypeReference<List<Order>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            orderList = new ArrayList<>();
        }
    }

    public void saveOrdersToFile() {
        try {
            String jsonString = objectMapper.writeValueAsString(orderList);
            Files.write(Paths.get(ORDERS_FILE), jsonString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Order> getAllOrders() {
        return orderList;
    }

    /**
     * Get only the pending orders if you need that specifically.
     * Or you can just filter them in the controller.
     */
    public List<Order> getAllPendingOrders() {
        List<Order> pending = new ArrayList<>();
        for (Order o : orderList) {
            if ("pending".equalsIgnoreCase(o.getStatus())) {
                pending.add(o);
            }
        }
        return pending;
    }

    public Optional<Order> findById(String id) {
        return orderList.stream()
                .filter(o -> o.getId().equals(id))
                .findFirst();
    }

    /**
     * Create (or update) an order in the list.
     * If an order with the same ID exists, we update it.
     */
    public Order saveOrder(Order order) {
        if (order.getId() == null || order.getId().isEmpty()) {
            order.setId(UUID.randomUUID().toString());
        }
        // Remove existing if same ID
        Optional<Order> existing = findById(order.getId());
        existing.ifPresent(orderList::remove);

        orderList.add(order);
        saveOrdersToFile();
        return order;
    }

    public boolean approveOrder(String id) {
        Optional<Order> existingOpt = findById(id);
        if (existingOpt.isPresent()) {
            Order order = existingOpt.get();
            order.setStatus("approved");
            saveOrdersToFile();
            return true;
        }
        return false;
    }

    public boolean rejectOrder(String id) {
        Optional<Order> existingOpt = findById(id);
        if (existingOpt.isPresent()) {
            Order order = existingOpt.get();
            order.setStatus("rejected");
            saveOrdersToFile();
            return true;
        }
        return false;
    }
}
