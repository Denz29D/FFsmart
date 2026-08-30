package com.example.FFsmartBackend.controllers;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FFsmartBackend.models.AuditLog;
import com.example.FFsmartBackend.models.User;
import com.example.FFsmartBackend.services.AuditLogService;
import com.example.FFsmartBackend.services.UserManagementService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private boolean userIsManagerOrHeadChef(User user) {
        if (user == null || user.getRole() == null) return false;
        String role = user.getRole();
        return role.equals("Manager") || role.equals("HeadChef");
    }

    private boolean userIsManager(User user) {
        return user != null && "Manager".equals(user.getRole());
    }

    // ----------------------------------------------------------------
    // GET /api/users
    // ----------------------------------------------------------------
    @GetMapping
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userIsManagerOrHeadChef(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        List<User> users = userManagementService.getAllUsers();

        // Log action
        auditLogService.addLog(new AuditLog(
                null,
                authUser.getId(),
                "FETCH_ALL_USERS",
                new Date(),
                "Manager or HeadChef retrieved all users"
        ));

        return ResponseEntity.ok(users);
    }

    // ----------------------------------------------------------------
    // GET /api/users/{id}
    // ----------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id,
                                         Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userIsManagerOrHeadChef(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        Optional<User> userOpt = userManagementService.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        // Log action
        auditLogService.addLog(new AuditLog(
                null,
                authUser.getId(),
                "FETCH_USER",
                new Date(),
                "User details retrieved for user ID: " + id
        ));

        return ResponseEntity.ok(userOpt.get());
    }

    // ----------------------------------------------------------------
    // POST /api/users
    // ----------------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> userRequest,
                                        Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userIsManager(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only managers can create users"));
        }

        String fullName = userRequest.get("fullName");
        String username = userRequest.get("username");
        String email = userRequest.get("email");
        String password = userRequest.get("password");
        String role = userRequest.get("role");

        if (fullName == null || username == null || email == null || password == null || role == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
        }

        if (userManagementService.userExists(username)) {
            return ResponseEntity.status(409).body(Map.of("error", "Username already exists"));
        }

        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(
                UUID.randomUUID().toString(),
                username,
                fullName,
                email,
                hashedPassword,
                role,
                null
        );

        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
                authUser.getId(),
                "CREATED_USER",
                new Date(),
                "User created: " + newUser.getUsername()
        ));

        userManagementService.createUser(newUser);
        return ResponseEntity.status(201).body(Map.of("message", "User created successfully"));
    }

    // ----------------------------------------------------------------
    // PUT /api/users/{id}
    // ----------------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id,
                                        @RequestBody Map<String, Object> userUpdate,
                                        Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userIsManagerOrHeadChef(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
    
        Optional<User> existingUserOpt = userManagementService.findById(id);
        if (existingUserOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
    
        User existingUser = existingUserOpt.get();
    
        if (userUpdate.containsKey("fullName")) {
            existingUser.setFullName((String) userUpdate.get("fullName"));
        }
        if (userUpdate.containsKey("username")) {
            existingUser.setUsername((String) userUpdate.get("username"));
        }
        if (userUpdate.containsKey("email")) {
            existingUser.setEmail((String) userUpdate.get("email"));
        }
        if (userUpdate.containsKey("role") && userIsManager(authUser)) {
            existingUser.setRole((String) userUpdate.get("role"));
        }
        if (userUpdate.containsKey("password") && !((String) userUpdate.get("password")).isBlank()) {
            existingUser.setPassword(passwordEncoder.encode((String) userUpdate.get("password")));
        }
        if (userUpdate.containsKey("permissions")) {
            Map<String, Boolean> permissions = (Map<String, Boolean>) userUpdate.get("permissions");
            existingUser.setPermissions(permissions);
        }
        
    
        userManagementService.createUser(existingUser);
    
        // Log action
        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
            authUser.getId(),
            "UPDATED_USER",
            new Date(),
            "Updated details for user ID: " + id
        ));
    
        return ResponseEntity.ok(Map.of("message", "User updated successfully"));
    }
    

    // ----------------------------------------------------------------
    // DELETE /api/users/{id}
    // ----------------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id,
                                        Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        if (!userIsManager(authUser)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only managers can delete users"));
        }

        Optional<User> userOpt = userManagementService.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        userManagementService.deleteUser(id);

        // Log action
        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
                authUser.getId(),
                "DELETED_USER",
                new Date(),
                "Deleted user with ID: " + id
        ));

        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // ----------------------------------------------------------------
    // GET /api/users/roles
    // ----------------------------------------------------------------
    @GetMapping("/roles")
    public ResponseEntity<List<String>> getAvailableRoles() {
        return ResponseEntity.ok(List.of("Manager", "HeadChef", "Chef", "Delivery"));
    }
}
