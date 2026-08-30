package com.example.FFsmartBackend.controllers;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.example.FFsmartBackend.models.Delivery;
import com.example.FFsmartBackend.models.User;
import com.example.FFsmartBackend.services.AuditLogService;
import com.example.FFsmartBackend.services.DeliveryService;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Only Delivery personnel have access
     */
    private boolean userIsDelivery(User user) {
        if (user == null || user.getRole() == null) return false;
        String role = user.getRole();
        return role.equals("Manager")
            || role.equals("HeadChef")
            || role.equals("Chef")
            || role.equals("Delivery")
            || role.equals("HealthAndSafetyOfficer")
            ;
    }

    // ----------------------------------------------------------------
    // GET /api/delivery
    // Get all deliveries (Delivery role only)
    // ----------------------------------------------------------------
    @GetMapping
    public ResponseEntity<?> getAllDeliveries(Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userIsDelivery(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied - Delivery personnel only"));
        }

        List<Delivery> all = deliveryService.getAllDeliveries();
        
        // Log audit entry
        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
                authUser.getId(),
                "VIEW_ALL_DELIVERIES",
                new Date(),
                "Viewed all delivery records"
        ));

        return ResponseEntity.ok(all);
    }

    // ----------------------------------------------------------------
    // POST /api/delivery
    // Log a new delivery (Delivery role only)
    // ----------------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> createDelivery(@RequestBody Map<String, Object> deliveryRequest,
                                            Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userIsDelivery(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied - Delivery personnel only"));
        }
    
        // Extract fields
        String itemName = (String) deliveryRequest.get("itemName");
        Integer quantity = (Integer) deliveryRequest.get("quantity");
        if (itemName == null || quantity == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing itemName or quantity"));
        }
    
        // Build Delivery object
        Delivery delivery = new Delivery();
        delivery.setItemName(itemName);
        delivery.setQuantity(quantity);
        delivery.setDeliveredBy(authUser.getId()); // or username
        delivery.setDeliveryDate(new Date());      // current date
        delivery.setApprovalStatus("Pending");     // Set status to pending on creation
    
        Delivery created = deliveryService.createDelivery(delivery);
    
        // Log audit entry
        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
            authUser.getId(),
            "CREATE_DELIVERY",
            new Date(),
            "Created new delivery: " + itemName + ", quantity: " + quantity
        ));
    
        return ResponseEntity.status(201).body(created);
    }
    

    // ----------------------------------------------------------------
    // GET /api/delivery/{id}
    // Get details of a specific delivery (Delivery role only)
    // ----------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<?> getDeliveryById(@PathVariable String id,
                                             Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userIsDelivery(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied - Delivery personnel only"));
        }

        Optional<Delivery> deliveryOpt = deliveryService.findById(id);
        if (deliveryOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Delivery not found"));
        }

        // Log audit entry
        auditLogService.addLog(new AuditLog(
                UUID.randomUUID().toString(),
                authUser.getId(),
                "VIEW_DELIVERY_DETAILS",
                new Date(),
                "Viewed delivery details for ID: " + id
        ));

        return ResponseEntity.ok(deliveryOpt.get());
    }
    // PUT /api/delivery/{id}/approve
@PutMapping("/{id}/approve")
public ResponseEntity<?> approveDelivery(@PathVariable String id, Authentication authentication) {
    User authUser = (User) authentication.getPrincipal();

    if (!List.of("HeadChef", "Manager").contains(authUser.getRole())) {
    return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
}

    Optional<Delivery> deliveryOpt = deliveryService.findById(id);
    if (deliveryOpt.isEmpty()) {
        return ResponseEntity.status(404).body(Map.of("error", "Delivery not found"));
    }

    Delivery delivery = deliveryOpt.get();
    delivery.setApprovalStatus("Approved");
    deliveryService.updateDelivery(delivery);

    auditLogService.addLog(new AuditLog(
        UUID.randomUUID().toString(),
        authUser.getId(),
        "APPROVE_DELIVERY",
        new Date(),
        "Approved delivery ID: " + id
    ));

    return ResponseEntity.ok(Map.of("message", "Delivery approved successfully"));
}

// POST /api/delivery/{id}/decline
@PutMapping("/{id}/decline")
public ResponseEntity<?> declineDelivery(@PathVariable String id, Authentication authentication) {
    User authUser = (User) authentication.getPrincipal();

      if (!List.of("HeadChef", "Manager").contains(authUser.getRole())) {
        return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
    }

    Optional<Delivery> deliveryOpt = deliveryService.findById(id);
    if (deliveryOpt.isEmpty()) {
        return ResponseEntity.status(404).body(Map.of("error", "Delivery not found"));
    }

    Delivery delivery = deliveryOpt.get();
    delivery.setApprovalStatus("Declined");
    deliveryService.updateDelivery(delivery);

    auditLogService.addLog(new AuditLog(
        UUID.randomUUID().toString(),
        authUser.getId(),
        "DECLINE_DELIVERY",
        new Date(),
        "Declined delivery ID: " + id
    ));

    return ResponseEntity.ok(Map.of("message", "Delivery declined successfully"));
}

}
