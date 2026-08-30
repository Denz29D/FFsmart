package com.example.FFsmartBackend.services;

import com.example.FFsmartBackend.models.RolePermissions;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class SystemSettingsService {

    private static final String PERMISSIONS_FILE = "src/main/resources/data/permissions.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<RolePermissions> rolePermissionsList = new ArrayList<>();

    public SystemSettingsService() {
        loadPermissionsFromFile();
    }

    private void loadPermissionsFromFile() {
        try {
            if (!Files.exists(Paths.get(PERMISSIONS_FILE))) {
                Files.createDirectories(Paths.get(PERMISSIONS_FILE).getParent());
                Files.createFile(Paths.get(PERMISSIONS_FILE));
                Files.write(Paths.get(PERMISSIONS_FILE), "[]".getBytes());
            }

            byte[] jsonData = Files.readAllBytes(Paths.get(PERMISSIONS_FILE));
            rolePermissionsList = objectMapper.readValue(jsonData, new TypeReference<List<RolePermissions>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            rolePermissionsList = new ArrayList<>();
        }
    }

    private void savePermissionsToFile() {
        try {
            String json = objectMapper.writeValueAsString(rolePermissionsList);
            Files.write(Paths.get(PERMISSIONS_FILE), json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return all available RolePermissions structures.
     */
    public List<RolePermissions> getAllRolePermissions() {
        return rolePermissionsList;
    }

    /**
     * Update or create a RolePermissions entry for the given role.
     */
    public void updateRolePermissions(String role, Map<String, Boolean> permissions) {
        // find existing
        Optional<RolePermissions> existing = rolePermissionsList.stream()
                .filter(rp -> rp.getRole().equalsIgnoreCase(role))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setPermissions(permissions);
        } else {
            RolePermissions newEntry = new RolePermissions(role, permissions);
            rolePermissionsList.add(newEntry);
        }
        savePermissionsToFile();
    }
}
