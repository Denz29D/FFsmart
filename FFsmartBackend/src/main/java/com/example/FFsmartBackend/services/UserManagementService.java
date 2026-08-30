package com.example.FFsmartBackend.services;

import com.example.FFsmartBackend.models.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class UserManagementService {

    private static final String USERS_FILE = "src/main/resources/data/users.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private List<User> userList = new ArrayList<>();

    public UserManagementService() {
        loadUsersFromFile();
    }

    /**
     * Load users from the JSON file.
     */
    private void loadUsersFromFile() {
        try {
            byte[] jsonData = Files.readAllBytes(Paths.get(USERS_FILE));
            userList = objectMapper.readValue(jsonData, new TypeReference<List<User>>() {});
        } catch (IOException e) {
            userList = new ArrayList<>(); // Start with an empty list if the file doesn't exist
        }
    }

    /**
     * Save users to the JSON file.
     */
    private void saveUsersToFile() {
        try {
            String json = objectMapper.writeValueAsString(userList);
            Files.write(Paths.get(USERS_FILE), json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all users.
     */
    public List<User> getAllUsers() {
        return userList;
    }

    /**
     * Find a user by ID.
     */
    public Optional<User> findById(String id) {
        return userList.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    /**
     * Find a user by username (for login).
     */
    public Optional<User> findByUsername(String username) {
        return userList.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    /**
     * Create a new user with hashed password.
     */
    public User createUser(User user) {
        user.setId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Hash password
        userList.add(user);
        saveUsersToFile();
        return user;
    }

    /**
     * Update user details.
     */
    public Optional<User> updateUser(String id, Map<String, String> updates) {
        Optional<User> existingUser = findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();

            if (updates.containsKey("fullName")) user.setFullName(updates.get("fullName"));
            if (updates.containsKey("username")) user.setUsername(updates.get("username"));
            if (updates.containsKey("email")) user.setEmail(updates.get("email"));
            if (updates.containsKey("role")) user.setRole(updates.get("role"));
            if (updates.containsKey("password")) {
                user.setPassword(passwordEncoder.encode(updates.get("password"))); // Hash new password
            }

            saveUsersToFile();
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /**
     * Delete a user.
     */
    public boolean deleteUser(String id) {
        Optional<User> existingUser = findById(id);
        if (existingUser.isPresent()) {
            userList.remove(existingUser.get());
            saveUsersToFile();
            return true;
        }
        return false;
    }

    /**
     * Get all available roles.
     */
    public List<String> getAllRoles() {
        return Arrays.asList("Manager", "HeadChef", "Chef", "Delivery");
    }

    /**
     * Check if a username exists.
     */
    public boolean userExists(String username) {
        return findByUsername(username).isPresent();
    }
}
