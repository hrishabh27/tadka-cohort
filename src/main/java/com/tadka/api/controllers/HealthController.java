package com.tadka.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getLiveness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Healthy");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> getReadiness() {
        long start = System.currentTimeMillis();
        boolean isDbHealthy = false;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
            isDbHealthy = true;
        } catch (Exception ignored) {
            isDbHealthy = false;
        }

        long responseTimeMs = System.currentTimeMillis() - start;

        Map<String, Object> dbCheck = new HashMap<>();
        dbCheck.put("status", isDbHealthy ? "healthy" : "unhealthy");
        dbCheck.put("responseTimeMs", responseTimeMs);

        Map<String, Object> checks = new HashMap<>();
        checks.put("database", dbCheck);

        Map<String, Object> response = new HashMap<>();
        response.put("status", isDbHealthy ? "healthy" : "unhealthy");
        response.put("timestamp", Instant.now().toString());
        response.put("checks", checks);

        if (isDbHealthy) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}
