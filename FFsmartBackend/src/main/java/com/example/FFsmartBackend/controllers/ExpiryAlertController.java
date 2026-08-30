package com.example.FFsmartBackend.controllers;

import com.example.FFsmartBackend.models.Alert;
import com.example.FFsmartBackend.models.User;
import com.example.FFsmartBackend.services.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class ExpiryAlertController {

    @Autowired
    private AlertService alertService;

    private boolean userHasAlertAccess(User user) {
        if (user == null || user.getRole() == null) return false;
        String role = user.getRole();
        return role.equals("HeadChef") ||
         role.equals("Chef") || role.equals("Manager");
    }

    @GetMapping
    public ResponseEntity<?> getAllAlerts(Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasAlertAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateAlerts(Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasAlertAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        alertService.generateExpiryAlerts();
        return ResponseEntity.ok(Map.of("message", "Expiry alerts generated successfully"));
    }

    @PostMapping("/acknowledge/{id}")
    public ResponseEntity<?> acknowledgeAlert(@PathVariable String id, Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasAlertAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        alertService.acknowledgeAlert(id);
        return ResponseEntity.ok(Map.of("message", "Alert acknowledged"));
    }
}
