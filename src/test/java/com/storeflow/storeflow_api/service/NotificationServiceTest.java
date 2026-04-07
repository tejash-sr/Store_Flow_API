package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.dto.InventoryAlertDTO;
import com.storeflow.storeflow_api.entity.InventoryItem;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.entity.Store;
import com.storeflow.storeflow_api.repository.InventoryItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService.
 * Tests WebSocket/STOMP notification delivery.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
public class NotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Store testStore;
    private Product testProduct;
    private InventoryItem lowStockItem;
    private InventoryItem outOfStockItem;

    @BeforeEach
    public void setup() {
        testStore = Store.builder()
            .id(1L)
            .name("Test Store")
            .address("123 Test St")
            .build();

        testProduct = Product.builder()
            .id(100L)
            .name("Test Product")
            .isActive(true)
            .build();

        lowStockItem = InventoryItem.builder()
            .id(1L)
            .product(testProduct)
            .store(testStore)
            .quantityOnHand(5L)
            .minimumStockLevel(10L)
            .build();

        outOfStockItem = InventoryItem.builder()
            .id(2L)
            .product(testProduct)
            .store(testStore)
            .quantityOnHand(0L)
            .minimumStockLevel(10L)
            .build();
    }

    @Test
    @DisplayName("Should notify all clients when low-stock items are found")
    public void testNotifyLowStockItems() {
        when(inventoryItemRepository.findBelowMinimumStockLevel())
            .thenReturn(new ArrayList<>(List.of(lowStockItem)));

        List<InventoryItem> result = notificationService.notifyLowStockItems();

        assertThat(result).hasSize(1);
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/alerts"),
            anyString()
        );
    }

    @Test
    @DisplayName("Should broadcast alert to all subscribed clients")
    public void testBroadcastAlert() {
        notificationService.broadcastAlert("System alert");

        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/alerts"),
            eq("System alert")
        );
    }
}
