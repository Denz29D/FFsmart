package com.example.FFsmartBackend.models;

import java.util.Map;

/**
 * RolePermissions holds a mapping from role -> permissionKey -> boolean
 * Example: 
 *   "Manager" -> {"canCreateUser": true, "canDeleteUser": false}
 */
public class RolePermissions {

    private String role;                   // e.g. "Manager"
    private Map<String, Boolean> permissions; // e.g. {"canCreateUser": true, "canDeleteUser": false}

    public RolePermissions() {
    }

    public RolePermissions(String role, Map<String, Boolean> permissions) {
        this.role = role;
        this.permissions = permissions;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }
}
