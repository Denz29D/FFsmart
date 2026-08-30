package com.example.FFsmartBackend.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.example.FFsmartBackend.models.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final String USERS_FILE = "src/main/resources/data/users.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<User> userList = new ArrayList<>();

    public UserService() {
        loadUsersFromFile();
    }

    private void loadUsersFromFile() {
        System.out.println("Loading users from file: " + USERS_FILE);
        try {
            // Check if file exists, create it if it doesn't
            if (!Files.exists(Paths.get(USERS_FILE))) {
                Files.createDirectories(Paths.get(USERS_FILE).getParent()); // Ensure parent directories exist
                Files.createFile(Paths.get(USERS_FILE)); // Create the file
                Files.write(Paths.get(USERS_FILE), "[]".getBytes()); // Write an empty JSON array
            }

            // Read file content
            byte[] jsonData = Files.readAllBytes(Paths.get(USERS_FILE));
            userList = objectMapper.readValue(jsonData, new TypeReference<List<User>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            userList = new ArrayList<>();
        }
    }

    private void saveUsersToFile() {
        try {
            String json = objectMapper.writeValueAsString(userList);
            Files.write(Paths.get(USERS_FILE), json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<User> findByUsername(String username) {
        return userList.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public Optional<User> findById(String id) {
        return userList.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public void save(User user) {
        var existing = findById(user.getId());
        if (existing.isPresent()) {
            userList.remove(existing.get());
        }
        userList.add(user);
        saveUsersToFile();
    }

    public boolean userExists(String username) {
        return findByUsername(username).isPresent();
    }
}
