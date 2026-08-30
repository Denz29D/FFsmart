package com.example.FFsmartBackend.services;

import com.example.FFsmartBackend.models.AuditLog;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class AuditLogService {

    private static final String LOG_FILE = "src/main/resources/data/auditlogs.json";
    private static final Logger LOGGER = Logger.getLogger(AuditLogService.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<AuditLog> logList = new ArrayList<>();

    public AuditLogService() {
        loadLogsFromFile();
    }

    private void loadLogsFromFile() {
        try {
            if (!Files.exists(Paths.get(LOG_FILE))) {
                Files.createDirectories(Paths.get(LOG_FILE).getParent());
                Files.createFile(Paths.get(LOG_FILE));
                Files.write(Paths.get(LOG_FILE), "[]".getBytes());
            }
            byte[] jsonData = Files.readAllBytes(Paths.get(LOG_FILE));
            logList = objectMapper.readValue(jsonData, new TypeReference<List<AuditLog>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            logList = new ArrayList<>();
        }
    }

    private void saveLogsToFile() {
        try {
            String json = objectMapper.writeValueAsString(logList);
            Files.write(Paths.get(LOG_FILE), json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<AuditLog> getAllLogs() {
        return logList;
    }

    public Optional<AuditLog> findById(String id) {
        return logList.stream().filter(log -> log.getId().equals(id)).findFirst();
    }

    /**
     * Add a new log entry. If log.id is null, generate one.
     */

     public AuditLog addLog(AuditLog log) {

        try {
            if (log.getId() == null || log.getId().isEmpty()) {
                log.setId(UUID.randomUUID().toString());
            }
            logList.add(log);
            saveLogsToFile();
            return log;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save audit log. Details: " + log.toString(), e);
            return null;  
        }
    }

    public List<AuditLog> getLogsByUserId(String userId) {
        List<AuditLog> result = new ArrayList<>();
        for (AuditLog al : logList) {
            if (al.getUserId() != null && al.getUserId().equals(userId)) {
                result.add(al);
            }
        }
        return result;
    }
}
