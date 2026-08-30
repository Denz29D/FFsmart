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
import com.example.FFsmartBackend.models.RolePermissions;
import com.example.FFsmartBackend.models.User;
import com.example.FFsmartBackend.services.AuditLogService;
import com.example.FFsmartBackend.services.SystemSettingsService;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    @Autowired
    private SystemSettingsService systemSettingsService;

    @Autowired
    private AuditLogService auditLogService;

    // Admin-only
    private boolean userIsAdmin(User user) {
        return user != null
                && user.getRole() != null
                && user.getRole().equals("Admin");
    }

    // ----------------------------------------------
    // GET /api/settings/permissions
    // Get all available permissions (for all roles)
    // ----------------------------------------------
    @GetMapping("/permissions")
    public ResponseEntity<?> getAllPermissions(Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userIsAdmin(authUser)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Access denied - Admin only"));
        }

        List<RolePermissions> allPerms = systemSettingsService.getAllRolePermissions();

        // Log action
        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
                authUser.getId(),
                "FETCH_PERMISSIONS",
                new Date(),
                "Admin fetched all role permissions"
        ));

        return ResponseEntity.ok(allPerms);
    }

    // ----------------------------------------------
    // PUT /api/settings/permissions/{role}
    // Update permissions for a specific role
    // ----------------------------------------------
    @PutMapping("/permissions/{role}")
    public ResponseEntity<?> updatePermissionsForRole(@PathVariable String role,
                                                      @RequestBody Map<String, Boolean> permissions,
                                                      Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userIsAdmin(authUser)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Access denied - Admin only"));
        }

        // Update or create permissions
        systemSettingsService.updateRolePermissions(role, permissions);

        // Log action
        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
                authUser.getId(),
                "UPDATE_PERMISSIONS",
                new Date(),
                "Admin updated permissions for role: " + role
        ));

        return ResponseEntity.ok(Map.of("message", "Permissions updated for role: " + role));
    }

    // ----------------------------------------------
    // POST /api/settings/backup
    // Perform a manual system backup (stub)
    // ----------------------------------------------
    @PostMapping("/backup")
    public ResponseEntity<?> performBackup(Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userIsAdmin(authUser)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Access denied - Admin only"));
        }

        // Log action
        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
                authUser.getId(),
                "PERFORM_BACKUP",
                new Date(),
                "Admin initiated a system backup"
        ));

        return ResponseEntity.ok(Map.of("message", "System backup initiated (stub)"));
    }

    // ----------------------------------------------
    // GET /api/settings/audit
    // Retrieve all system activity logs
    // ----------------------------------------------
    @GetMapping("/audit")
    public ResponseEntity<?> getSystemAuditLogs(Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userIsAdmin(authUser)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Access denied - Admin only"));
        }

        // Log action
        auditLogService.addLog(new AuditLog(
                UUID.randomUUID().toString(),
                authUser.getId(),
                "FETCH_AUDIT_LOGS",
                new Date(),
                "Admin retrieved system audit logs"
        ));

        return ResponseEntity.ok(auditLogService.getAllLogs());
    }
}
