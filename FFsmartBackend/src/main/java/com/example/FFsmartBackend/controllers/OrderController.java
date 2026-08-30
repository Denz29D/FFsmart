package com.example.FFsmartBackend.controllers;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FFsmartBackend.models.AuditLog;
import com.example.FFsmartBackend.models.Order;
import com.example.FFsmartBackend.models.User;
import com.example.FFsmartBackend.services.AuditLogService;
import com.example.FFsmartBackend.services.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuditLogService auditLogService;

    private boolean userHasOrderAccess(User user) {
        if (user == null || user.getRole() == null) return false;
        String role = user.getRole();
        return role.equals("HeadChef") || role.equals("Supplier");
    }

    /**
     * GET /api/orders
     * Return all pending orders (or all orders, if you prefer).
     */
    @GetMapping
    public ResponseEntity<?> getAllPendingOrders(Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasOrderAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        List<Order> orders = orderService.getAllPendingOrders();

        // Log fetching of orders
        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
                authUser.getId(),
                "FETCH_ORDERS",
                new Date(),
                "User fetched pending orders"
        ));

        return ResponseEntity.ok(orders);
    }

    /**
     * POST /api/orders
     * Create a new reorder request.
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> requestBody,
                                         Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasOrderAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
    
        // Validate the order fields
        String validationError = validateOrderRequest(requestBody);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(Map.of("error", validationError));
        }
    
        // Extracting fields after validation
        String itemName = (String) requestBody.get("itemName");
        Integer quantity = (Integer) requestBody.get("quantity");
    
        // Create the new order
        Order order = new Order();
        order.setItemName(itemName);
        order.setQuantity(quantity);
        order.setStatus("pending");
        order.setCreatedAt(new Date());
    
        Order savedOrder = orderService.saveOrder(order);
    
        // Log order creation
        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
            authUser.getId(),
            "CREATE_ORDER",
            new Date(),
            "User created an order for item: " + itemName + " (Quantity: " + quantity + ")"
        ));
    
        return ResponseEntity.status(201).body(savedOrder);
    }
    
    /**
     * Validates the order request body to ensure all required fields are present and valid.
     * Returns an error message if validation fails, or null if validation passes.
     */
    private String validateOrderRequest(Map<String, Object> requestBody) {
        if (requestBody.get("itemName") == null || ((String) requestBody.get("itemName")).isBlank()) {
            return "Item name is required.";
        }
        if (requestBody.get("quantity") == null || !(requestBody.get("quantity") instanceof Integer) || (Integer) requestBody.get("quantity") <= 0) {
            return "Quantity is required and must be a positive integer.";
        }
        return null; // Validation passed
    }
    
    /**
     * PUT /api/orders/{id}/approve
     * Approve a reorder request.
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveOrder(@PathVariable String id,
                                          Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasOrderAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        boolean success = orderService.approveOrder(id);
        if (!success) {
            return ResponseEntity.status(404).body(Map.of("error", "Order not found"));
        }

        // Log order approval
        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
                authUser.getId(),
                "APPROVE_ORDER",
                new Date(),
                "User approved order with ID: " + id
        ));

        return ResponseEntity.ok(Map.of("message", "Order approved"));
    }

    /**
     * PUT /api/orders/{id}/reject
     * Reject a reorder request.
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectOrder(@PathVariable String id,
                                         Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasOrderAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        boolean success = orderService.rejectOrder(id);
        if (!success) {
            return ResponseEntity.status(404).body(Map.of("error", "Order not found"));
        }

        // Log order rejection
        auditLogService.addLog(new AuditLog(
                UUID.randomUUID().toString(),
                authUser.getId(),
                "REJECT_ORDER",
                new Date(),
                "User rejected order with ID: " + id
        ));

        return ResponseEntity.ok(Map.of("message", "Order rejected"));
    }
}
