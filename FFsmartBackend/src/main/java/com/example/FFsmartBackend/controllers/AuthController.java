package com.example.FFsmartBackend.controllers;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FFsmartBackend.lib.utils.JwtService;
import com.example.FFsmartBackend.models.AuditLog;
import com.example.FFsmartBackend.models.User;
import com.example.FFsmartBackend.services.AuditLogService;
import com.example.FFsmartBackend.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditLogService auditLogService;

    // ==================================================================
    // ================         LOGIN ENDPOINT         ===================
    // ==================================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(
        @RequestBody Map<String, String> loginRequest,
        HttpServletResponse response
    ) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
    
        Optional<User> userOpt = userService.findByUsername(username);
    
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                String jwtToken = jwtService.generateToken(user.getId(), user.getRole());
    
                // Only set 'Secure' if you’re on HTTPS (i.e., production)
                boolean isProd = false; // or read from environment
                Cookie jwtCookie = new Cookie("jwt", jwtToken);
                jwtCookie.setPath("/");
                jwtCookie.setHttpOnly(true);
                jwtCookie.setSecure(isProd); // In local dev, set this to 'false'
                jwtCookie.setMaxAge((int) (jwtService.getJwtExpirationMs() / 1000));
                response.addCookie(jwtCookie);
    
                // Log successful login
                auditLogService.addLog(new AuditLog(
                    UUID.randomUUID().toString(),
                    user.getId(),
                    "LOGIN",
                    new Date(),
                    "User logged in successfully: " + username
                ));
    
                return ResponseEntity.ok(Map.of("message", "Login successful"));
            }
        }
        return ResponseEntity.status(401).body("Invalid username or password");
    }
    

    // ==================================================================
    // ================         SIGNUP ENDPOINT         ==================
    // ==================================================================
   @PostMapping("/signup")
public ResponseEntity<?> signup(@Valid @RequestBody Map<String, String> signupRequest) {
    String fullName = signupRequest.get("fullName");
    String username = signupRequest.get("username");
    String email = signupRequest.get("email");
    String password = signupRequest.get("password");
    String role = signupRequest.get("role");

    if (fullName == null || username == null || email == null || password == null || role == null) {
        return ResponseEntity.status(400).body(Map.of("error", "Missing required fields"));
    }

    if (userService.userExists(username)) {
        return ResponseEntity.status(409).body(Map.of("error", "Username already exists"));
    }

    try {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(password));

        // Assign default permissions for Health and Safety Officers
        if (role.equals("HealthAndSafetyOfficer")) {
            Map<String, Boolean> permissions = new HashMap<>();
            permissions.put("User can't add/remove items", true);
            user.setPermissions(permissions);
        }

        userService.save(user);

        // Log successful signup
        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
                user.getId(),
                "SIGNUP",
                new Date(),
                "New user signed up: " + username
        ));

        return ResponseEntity.status(201).body(Map.of("message", "User created successfully"));
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error", "An error occurred while creating the user"));
    }
}


    // ==================================================================
    // ================         ME ENDPOINT            ===================
    // ==================================================================
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@CookieValue(value = "jwt", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized - Missing token");
        }
    
        String userId = jwtService.validateTokenAndGetUserId(token);
        if (userId == null) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    
        Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
    
        User realUser = userOptional.get();
        
        // Create a detached copy so we never overwrite the real password with null
        User userCopy = new User();
        userCopy.setId(realUser.getId());
        userCopy.setFullName(realUser.getFullName());
        userCopy.setUsername(realUser.getUsername());
        userCopy.setEmail(realUser.getEmail());
        userCopy.setRole(realUser.getRole());
        userCopy.setPermissions(realUser.getPermissions());
        // Notice we do NOT copy the password—this ensures it stays null in the response only
        
        // Log access to user profile
        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
            userId,
            "VIEW_PROFILE",
            new Date(),
            "User viewed their profile: " + realUser.getUsername()
        ));
    
        try {
            // Return only the detached copy without its password
            return ResponseEntity.ok(userCopy);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing user data");
        }
    }
    
    // ==================================================================
    // ================         LOGOUT ENDPOINT         ==================
    // ==================================================================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        // Log logout event
        auditLogService.addLog(new AuditLog(
            UUID.randomUUID().toString(),
                "UNKNOWN_USER",  // Can be updated if user info is available during logout
                "LOGOUT",
                new Date(),
                "User logged out"
        ));

        return ResponseEntity.ok("Logged out successfully");
    }
}
