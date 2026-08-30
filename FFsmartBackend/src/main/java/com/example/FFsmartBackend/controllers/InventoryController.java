package com.example.FFsmartBackend.controllers;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.FFsmartBackend.models.AuditLog;
import com.example.FFsmartBackend.models.Inventory;
import com.example.FFsmartBackend.models.User;
import com.example.FFsmartBackend.services.AlertService;
import com.example.FFsmartBackend.services.AuditLogService;
import com.example.FFsmartBackend.services.InventoryService;
import com.example.FFsmartBackend.services.ReportService;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ReportService reportService;

    /**
     * Utility method to ensure that only Chef, HeadChef, Delivery, or Manager can access.
     */
    private boolean userHasInventoryAccess(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        String role = user.getRole();
        return role.equals("Chef")
                || role.equals("HeadChef")
                || role.equals("Delivery")
                || role.equals("Manager") || role.equals("HealthAndSafetyOfficer"); // Added role
            
                
    }

    // ----------------------------------------------------------------
    // GET /api/inventory
    // ----------------------------------------------------------------
    @GetMapping
    public ResponseEntity<?> getAllInventory(Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasInventoryAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
    
        // Generate expiry alerts before returning inventory data
        alertService.generateExpiryAlerts(); 
    
        List<Inventory> items = inventoryService.getAllItems();
        return ResponseEntity.ok(items);
    }


    
    // ----------------------------------------------------------------
    // POST /api/inventory (Add new item)
    // ----------------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> addInventoryItem(@RequestBody Inventory newItem, Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasInventoryAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
    
        // Validate all required attributes
        String validationError = validateInventory(newItem);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(Map.of("error", validationError));
        }
    
        Inventory created = inventoryService.createItem(newItem);
    
        // Add an audit log entry
        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
            authUser.getId(),
            "ADD_INVENTORY_ITEM",
            new Date(),
            "Added new item: " + newItem.getItemName() + " (ID: " + created.getId() + ")"
        ));
    
        return ResponseEntity.status(201).body(created);
    }
    
    /**
     * Validates the Inventory object to ensure all required fields are present and valid.
     * Returns an error message if validation fails, or null if validation passes.
     */
    private String validateInventory(Inventory item) {
        if (item.getItemName() == null || item.getItemName().isBlank()) {
            return "Item name is required.";
        }
        if (item.getQuantity() == null || item.getQuantity() < 0) {
            return "Quantity is required and must be non-negative.";
        }
        if (item.getType() == null || item.getType().isBlank()) {
            return "Type is required.";
        }
        if (item.getExpiryDate() == null) {
            return "Expiry date is required.";
        }
        if (item.getThresholdQuantity() == null || item.getThresholdQuantity() < 0) {
            return "Threshold quantity is required and must be non-negative.";
        }
        if (item.getFridgeLocation() == null || item.getFridgeLocation().isBlank()) {
            return "Fridge location is required.";
        }
        return null; // All validations passed
    }
    
    // ----------------------------------------------------------------
    // PUT /api/inventory/{id} (Update item)
    // ----------------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInventoryItem(@PathVariable String id, @RequestBody Inventory updatedItem, Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasInventoryAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        boolean updated = inventoryService.updateItem(id, updatedItem);
        if (!updated) {
            return ResponseEntity.status(404).body(Map.of("error", "Item not found"));
        }

        // Add an audit log entry
        auditLogService.addLog(new AuditLog(
                UUID.randomUUID().toString(),
                authUser.getId(),
                "UPDATE_INVENTORY_ITEM",
                new Date(),
                "Updated item: " + updatedItem.getItemName() + " (ID: " + id + ")"
        ));

        return ResponseEntity.ok(Map.of("message", "Item updated successfully"));
    }

    // ----------------------------------------------------------------
    // DELETE /api/inventory/{id} (Delete item)
    // ----------------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInventoryItem(@PathVariable String id, Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasInventoryAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        boolean deleted = inventoryService.deleteItem(id);
        if (!deleted) {
            return ResponseEntity.status(404).body(Map.of("error", "Item not found"));
        }

        // Add an audit log entry
        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
                authUser.getId(),
                "DELETE_INVENTORY_ITEM",
                new Date(),
                "Deleted item with ID: " + id
        ));

        return ResponseEntity.ok(Map.of("message", "Item deleted successfully"));
    }

    // ----------------------------------------------------------------
    // GET /api/inventory/low-stock
    // ----------------------------------------------------------------
    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStockItems(Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasInventoryAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    // ----------------------------------------------------------------
    // GET /api/inventory/expiring
    // ----------------------------------------------------------------
    @GetMapping("/expiring")
    public ResponseEntity<?> getExpiringItems(Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasInventoryAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        return ResponseEntity.ok(inventoryService.getExpiringSoon());
    }

    // ----------------------------------------------------------------
    // GET /api/inventory/{id} (Fetch specific item)
    // ----------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<?> getInventoryById(@PathVariable String id, Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasInventoryAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        Optional<Inventory> itemOpt = inventoryService.findById(id);
        if (itemOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Item not found"));
        }
        return ResponseEntity.ok(itemOpt.get());
    }

    // ----------------------------------------------------------------
    // Generate CSV Report
    // ----------------------------------------------------------------
    public ResponseEntity<?> generateCsvReport(@RequestParam(required = false) String startDate,
                                               @RequestParam(required = false) String endDate,
                                               Authentication authentication) throws IOException {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasInventoryAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        List<Inventory> inventoryList = inventoryService.getAllItems();

        StringWriter writer = new StringWriter();
        reportService.generateCsvReport(writer, inventoryList);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(writer.toString());
    }
}
