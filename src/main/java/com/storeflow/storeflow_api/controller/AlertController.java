package com.storeflow.storeflow_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.storeflow.storeflow_api.dto.InventoryAlertDTO;
import com.storeflow.storeflow_api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for real-time inventory alert management.
 * 
 * Provides endpoints for:
 * - Sending real-time alerts to WebSocket clients
 * - Managing alert subscriptions
 * - Broadcasting system notifications
 * - Retrieving alert status
 * 
 * All alerts are broadcast via WebSocket STOMP messaging to subscribed clients.
 * 
 * @author StoreFlow
 * @version 1.0
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Alerts", description = "Real-time inventory alerts and notifications via WebSocket")
public class AlertController {

    private final NotificationService notificationService;

    /**
     * POST /api/alerts/notify/low-stock - Broadcast all low-stock alerts.
     * 
     * Scans inventory and sends alerts for all items below minimum stock level
     * to all connected WebSocket clients.
     * 
     * @return count of items that triggered low-stock alerts
     */
    @PostMapping("/notify/low-stock")
    public ResponseEntity<?> notifyLowStock() {
        var lowStockItems = notificationService.notifyLowStockItems();
        
        return ResponseEntity.ok()
            .body(new AlertResponse(
                "LOW_STOCK_NOTIFICATION_SENT",
                "Low stock alerts broadcasted to all subscribed clients",
                lowStockItems.size()
            ));
    }

    /**
     * POST /api/alerts/notify/out-of-stock - Broadcast all out-of-stock alerts.
     * 
     * Scans inventory and sends critical alerts for all items with zero quantity
     * to all connected WebSocket clients.
     * 
     * @return count of out-of-stock items
     */
    @PostMapping("/notify/out-of-stock")
    public ResponseEntity<?> notifyOutOfStock() {
        var outOfStockItems = notificationService.notifyOutOfStockItems();
        
        return ResponseEntity.ok()
            .body(new AlertResponse(
                "OUT_OF_STOCK_NOTIFICATION_SENT",
                "Out-of-stock alerts broadcasted to all subscribed clients",
                outOfStockItems.size()
            ));
    }

    /**
     * POST /api/alerts/broadcast - Send a custom alert to all subscribed clients.
     * 
     * Allows system administrators to broadcast custom alert messages
     * to all connected WebSocket clients subscribed to /topic/alerts.
     * 
     * @param alert the alert object to broadcast
     * @return confirmation message
     */
    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcastAlert(@RequestBody InventoryAlertDTO alert) {
        if (alert.getMessage() == null || alert.getMessage().isBlank()) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_REQUEST", "Alert message cannot be empty"));
        }

        notificationService.broadcastAlert(alert);
        
        return ResponseEntity.ok()
            .body(new AlertResponse(
                "BROADCAST_SENT",
                "Alert successfully broadcasts to all subscribed clients",
                1
            ));
    }

    /**
     * POST /api/alerts/notify-user - Send alert to specific user.
     * 
     * Sends a user-specific alert to a particular user's private queue
     * via WebSocket /queue/notifications destination.
     * 
     * @param username the target username
     * @param alert the alert to send to user
     * @return confirmation message
     */
    @PostMapping("/notify-user/{username}")
    public ResponseEntity<?> notifyUser(
            @PathVariable String username,
            @RequestBody InventoryAlertDTO alert) {

        notificationService.notifyUserAlert(username, alert);
        
        return ResponseEntity.ok()
            .body(new AlertResponse(
                "USER_ALERT_SENT",
                String.format("Alert sent to user: %s", username),
                1
            ));
    }

    /**
     * GET /api/alerts/check-all - Perform full inventory check and notify.
     * 
     * Comprehensive health check endpoint that:
     * - Scans all low-stock items and notifies
     * - Scans all out-of-stock items and notifies
     * - Returns summary of alert status
     * 
     * Useful for periodic intervals (e.g., every 5 minutes) to ensure
     * all conditions are being monitored.
     * 
     * @return summary of items needing attention
     */
    @GetMapping("/check-all")
    public ResponseEntity<?> checkAllInventory() {
        var lowStockItems = notificationService.checkAndNotifyLowStock();
        var outOfStockItems = notificationService.checkAndNotifyOutOfStock();
        
        return ResponseEntity.ok()
            .body(new InventoryCheckResponse(
                lowStockItems.size(),
                outOfStockItems.size(),
                lowStockItems.size() + outOfStockItems.size()
            ));
    }

    /**
     * GET /api/alerts/status - Get current alert system status.
     * 
     * Returns status information about the alert system including
     * configuration and connectivity details.
     * 
     * @return alert system status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getAlertStatus() {
        return ResponseEntity.ok()
            .body(new StatusResponse(
                "ALERT_SYSTEM_OPERATIONAL",
                "Real-time inventory alert system is operational",
                "/topic/alerts",
                "/queue/notifications",
                true
            ));
    }

    /**
     * GET /api/alerts/health - Health check for alert system.
     * 
     * Simple endpoint to verify alert service is operational.
     * 
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(new HealthResponse("UP", "Alert system is operational"));
    }

    /**
     * Response DTOs for alert endpoints
     */
    
    record AlertResponse(String type, String message, int count) {}
    record ErrorResponse(String error, String message) {}
    record InventoryCheckResponse(int lowStockCount, int outOfStockCount, int totalAlert) {}
    record StatusResponse(String status, String message, String broadcastTopic, String privateTopic, boolean connected) {}
    record HealthResponse(String status, String message) {}
}
