package com.example.FFsmartBackend.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.FFsmartBackend.models.AuditLog;
import com.example.FFsmartBackend.models.User;
import com.example.FFsmartBackend.services.AuditLogService;
import com.example.FFsmartBackend.services.UserService;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogService auditLogService;

    // Only Admin, HeadChef, H
    private boolean userHasAuditAccess(User user) {
        if (user == null || user.getRole() == null) return false;
        String role = user.getRole();
        return role.equals("Manager")
            || role.equals("HeadChef")
            || role.equals("HealthAndSafetyOfficer");
            
    }

    /**
     * GET /api/audit-logs
     * Retrieve all system audit logs.
     */
    @GetMapping
    public ResponseEntity<?> getAllAuditLogs(Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasAuditAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        List<AuditLog> logs = auditLogService.getAllLogs();
        return ResponseEntity.ok(logs);
    }

    /**
     * GET /api/audit-logs/{id}
     * Get details of a specific log entry.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAuditLogById(@PathVariable String id,
                                             Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasAuditAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        Optional<AuditLog> logOpt = auditLogService.findById(id);
        if (logOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Audit log not found"));
        }
        return ResponseEntity.ok(logOpt.get());
    }
      // ----------------------------------------------------------------
    // GET /api/audit-logs/filter
    // Filter logs by username (e.g. /api/audit-logs/filter?username=john)
    // ----------------------------------------------------------------
    @GetMapping("/filter")
    public ResponseEntity<?> getAuditLogsByUsername(@RequestParam("username") String username,
                                                    Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userHasAuditAccess(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        // 1) Find the user with that username
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            // If there's no user with that username, return empty or 404
            return ResponseEntity.ok(List.of()); 
            // or use 404: ResponseEntity.status(404).body(...);
        }

        // 2) Filter logs by that user's ID
        String userId = userOpt.get().getId();
        List<AuditLog> logs = auditLogService.getLogsByUserId(userId);

        return ResponseEntity.ok(logs);
    }

}
