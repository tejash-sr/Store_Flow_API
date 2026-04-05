package com.storeflow.storeflow_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "System health and status endpoints")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check application health", description = "Returns the current health status of the StoreFlow API, including uptime and timestamp")
    @ApiResponse(responseCode = "200", description = "API is healthy and operational")
    public ResponseEntity<Map<String, Object>> getHealth() {
        log.info("Health check endpoint called");
        
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("uptimeMs", uptimeMs);
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
