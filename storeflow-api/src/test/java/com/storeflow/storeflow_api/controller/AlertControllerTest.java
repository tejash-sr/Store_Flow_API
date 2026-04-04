package com.storeflow.storeflow_api.controller;

import com.storeflow.storeflow_api.dto.InventoryAlertDTO;
import com.storeflow.storeflow_api.entity.InventoryItem;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.entity.Store;
import com.storeflow.storeflow_api.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AlertController.
 * 
 * Tests verify:
 * - Low-stock alert notifications
 * - Out-of-stock alert notifications
 * - Custom alert broadcasting
 * - User-specific alert delivery
 * - Inventory health checks
 * - Alert system status endpoints
 * 
 * @author StoreFlow
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("AlertController Unit Tests")
public class AlertControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AlertController alertController;

    private Store testStore;
    private Product testProduct;
    private InventoryItem lowStockItem;
    private InventoryItem outOfStockItem;

    @BeforeEach
    public void setup() {
        testStore = Store.builder().id(1L).name("Test Store").build();
        testProduct = Product.builder().id(100L).name("Test Product").build();
        
        lowStockItem = InventoryItem.builder()
            .id(1L).product(testProduct).store(testStore)
            .quantityOnHand(5L).minimumStockLevel(10L)
            .build();

        outOfStockItem = InventoryItem.builder()
            .id(2L).product(testProduct).store(testStore)
            .quantityOnHand(0L).minimumStockLevel(10L)
            .build();
    }

    /**
     * Test 1: Verify POST /api/alerts/notify/low-stock broadcasts low-stock alerts.
     */
    @Test
    @DisplayName("Should notify about low-stock items via POST /notify/low-stock")
    public void testNotifyLowStock() {
        when(notificationService.notifyLowStockItems())
            .thenReturn(new ArrayList<>(List.of(lowStockItem)));

        ResponseEntity<?> response = alertController.notifyLowStock();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService, times(1)).notifyLowStockItems();
    }

    /**
     * Test 2: Verify no items returns empty response.
     */
    @Test
    @DisplayName("Should return empty response when no low-stock items found")
    public void testNotifyLowStockEmpty() {
        when(notificationService.notifyLowStockItems())
            .thenReturn(new ArrayList<>());

        ResponseEntity<?> response = alertController.notifyLowStock();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * Test 3: Verify POST /api/alerts/notify/out-of-stock broadcasts out-of-stock alerts.
     */
    @Test
    @DisplayName("Should notify about out-of-stock items via POST /notify/out-of-stock")
    public void testNotifyOutOfStock() {
        when(notificationService.notifyOutOfStockItems())
            .thenReturn(new ArrayList<>(List.of(outOfStockItem)));

        ResponseEntity<?> response = alertController.notifyOutOfStock();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService, times(1)).notifyOutOfStockItems();
    }

    /**
     * Test 4: Verify POST /api/alerts/broadcast sends custom alert.
     */
    @Test
    @DisplayName("Should broadcast custom alert message to all clients")
    public void testBroadcastAlert() {
        InventoryAlertDTO alert = InventoryAlertDTO.builder()
            .timestamp(LocalDateTime.now())
            .alertType("SYSTEM_MESSAGE")
            .message("System maintenance starting...")
            .build();

        ResponseEntity<?> response = alertController.broadcastAlert(alert);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService, times(1)).broadcastAlert(alert);
    }

    /**
     * Test 5: Verify empty message returns bad request.
     */
    @Test
    @DisplayName("Should return bad request for empty alert message")
    public void testBroadcastAlertEmpty() {
        InventoryAlertDTO alert = InventoryAlertDTO.builder()
            .message("")
            .build();

        ResponseEntity<?> response = alertController.broadcastAlert(alert);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * Test 6: Verify null message returns bad request.
     */
    @Test
    @DisplayName("Should return bad request for null alert message")
    public void testBroadcastAlertNull() {
        InventoryAlertDTO alert = InventoryAlertDTO.builder()
            .message(null)
            .build();

        ResponseEntity<?> response = alertController.broadcastAlert(alert);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * Test 7: Verify POST /api/alerts/notify-user sends user-specific alert.
     */
    @Test
    @DisplayName("Should send alert to specific user via POST /notify-user/{username}")
    public void testNotifyUser() {
        InventoryAlertDTO alert = InventoryAlertDTO.builder()
            .alertType("PERSONAL_NOTIFICATION")
            .message("Your reorder is due")
            .build();

        ResponseEntity<?> response = alertController.notifyUser("john.doe", alert);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService, times(1)).notifyUserAlert("john.doe", alert);
    }

    /**
     * Test 8: Verify GET /api/alerts/check-all performs comprehensive inventory check.
     */
    @Test
    @DisplayName("Should perform comprehensive inventory check via GET /check-all")
    public void testCheckAllInventory() {
        when(notificationService.checkAndNotifyLowStock())
            .thenReturn(new ArrayList<>(List.of(lowStockItem)));
        when(notificationService.checkAndNotifyOutOfStock())
            .thenReturn(new ArrayList<>(List.of(outOfStockItem)));

        ResponseEntity<?> response = alertController.checkAllInventory();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService, times(1)).checkAndNotifyLowStock();
        verify(notificationService, times(1)).checkAndNotifyOutOfStock();
    }

    /**
     * Test 9: Verify GET /api/alerts/status returns alert system status.
     */
    @Test
    @DisplayName("Should return alert system status via GET /status")
    public void testGetAlertStatus() {
        ResponseEntity<?> response = alertController.getAlertStatus();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    /**
     * Test 10: Verify GET /api/alerts/health performs health check.
     */
    @Test
    @DisplayName("Should return health check status via GET /health")
    public void testHealthCheck() {
        ResponseEntity<?> response = alertController.healthCheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
