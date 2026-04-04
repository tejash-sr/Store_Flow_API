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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for NotificationService.
 * 
 * Tests verify:
 * - Low-stock notifications are sent correctly
 * - Out-of-stock alerts are broadcast
 * - Inventory updates are communicated
 * - User-specific notifications are delivered
 * - Message formatting and DTO creation
 * 
 * @author StoreFlow
 * @version 1.0
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
        // Create test data
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

    /**
     * Test 1: Verify low-stock items notification is sent to clients.
     */
    @Test
    @DisplayName("Should notify all clients when low-stock items are found")
    public void testNotifyLowStockItems() {
        when(inventoryItemRepository.findBelowMinimumStockLevel())
            .thenReturn(new ArrayList<>(List.of(lowStockItem)));

        List<InventoryItem> result = notificationService.notifyLowStockItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/alerts"),
            anyString()
        );
    }

    /**
     * Test 2: Verify no notification when no low-stock items exist.
     */
    @Test
    @DisplayName("Should not send notification when no low-stock items exist")
    public void testNotifyLowStockItemsEmpty() {
        when(inventoryItemRepository.findBelowMinimumStockLevel())
            .thenReturn(new ArrayList<>());

        List<InventoryItem> result = notificationService.notifyLowStockItems();

        assertThat(result).isEmpty();
        verify(messagingTemplate, times(0)).convertAndSend(
            eq("/topic/alerts"),
            anyString()
        );
    }

    /**
     * Test 3: Verify out-of-stock notification is sent.
     */
    @Test
    @DisplayName("Should notify about out-of-stock items")
    public void testNotifyOutOfStockItems() {
        when(inventoryItemRepository.findOutOfStock())
            .thenReturn(new ArrayList<>(List.of(outOfStockItem)));

        List<InventoryItem> result = notificationService.notifyOutOfStockItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantityOnHand()).isZero();
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/alerts"),
            anyString()
        );
    }

    /**
     * Test 4: Verify empty list when no out-of-stock items exist.
     */
    @Test
    @DisplayName("Should return empty list when no out-of-stock items exist")
    public void testNotifyOutOfStockItemsEmpty() {
        when(inventoryItemRepository.findOutOfStock())
            .thenReturn(new ArrayList<>());

        List<InventoryItem> result = notificationService.notifyOutOfStockItems();

        assertThat(result).isEmpty();
    }

    /**
     * Test 5: Verify inventory update notification includes correct details.
     */
    @Test
    @DisplayName("Should send inventory update notification with correct store and product")
    public void testNotifyInventoryUpdate() {
        notificationService.notifyInventoryUpdate(1L, 100L, 50L);

        ArgumentCaptor<Object> alertCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/inventory"),
            alertCaptor.capture()
        );

        InventoryAlertDTO alert = (InventoryAlertDTO) alertCaptor.getValue();
        assertThat(alert.getStoreId()).isEqualTo(1L);
        assertThat(alert.getProductId()).isEqualTo(100L);
        assertThat(alert.getQuantity()).isEqualTo(50);
        assertThat(alert.getAlertType()).isEqualTo("INVENTORY_UPDATE");
    }

    /**
     * Test 6: Verify low-stock alert for specific item includes all details.
     */
    @Test
    @DisplayName("Should send detailed low-stock alert for specific inventory item")
    public void testNotifyLowStockForItem() {
        notificationService.notifyLowStockForItem(lowStockItem);

        ArgumentCaptor<Object> alertCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/alerts"),
            alertCaptor.capture()
        );

        InventoryAlertDTO alert = (InventoryAlertDTO) alertCaptor.getValue();
        assertThat(alert.getAlertType()).isEqualTo("LOW_STOCK");
        assertThat(alert.getQuantity()).isEqualTo(5);
        assertThat(alert.getMinimumLevel()).isEqualTo(10);
        assertThat(alert.getStoreId()).isEqualTo(1L);
        assertThat(alert.getProductId()).isEqualTo(100L);
    }

    /**
     * Test 7: Verify null inventory item is handled gracefully.
     */
    @Test
    @DisplayName("Should not send notification for null inventory item")
    public void testNotifyLowStockForItemNull() {
        notificationService.notifyLowStockForItem(null);

        verify(messagingTemplate, times(0)).convertAndSend(anyString(), anyString());
    }

    /**
     * Test 8: Verify out-of-stock alert marks item as critical.
     */
    @Test
    @DisplayName("Should mark out-of-stock alerts as critical")
    public void testNotifyOutOfStockForItem() {
        notificationService.notifyOutOfStockForItem(outOfStockItem);

        ArgumentCaptor<Object> alertCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/alerts"),
            alertCaptor.capture()
        );

        InventoryAlertDTO alert = (InventoryAlertDTO) alertCaptor.getValue();
        assertThat(alert.getAlertType()).isEqualTo("OUT_OF_STOCK");
        assertThat(alert.getCritical()).isTrue();
        assertThat(alert.getQuantity()).isZero();
    }

    /**
     * Test 9: Verify user-specific notification uses /queue destination.
     */
    @Test
    @DisplayName("Should send message to specific user via /queue/notifications")
    public void testNotifyUser() {
        notificationService.notifyUser("john.doe", "Test notification message");

        verify(messagingTemplate, times(1)).convertAndSendToUser(
            eq("john.doe"),
            eq("/queue/notifications"),
            eq("Test notification message")
        );
    }

    /**
     * Test 10: Verify user alert sends DTO to /queue destination.
     */
    @Test
    @DisplayName("Should send alert DTO to user via /queue/notifications")
    public void testNotifyUserAlert() {
        InventoryAlertDTO alert = InventoryAlertDTO.builder()
            .alertType("LOW_STOCK")
            .message("Test alert")
            .build();

        notificationService.notifyUserAlert("manager", alert);

        verify(messagingTemplate, times(1)).convertAndSendToUser(
            eq("manager"),
            eq("/queue/notifications"),
            eq(alert)
        );
    }

    /**
     * Test 11: Verify broadcast alert sends to all clients.
     */
    @Test
    @DisplayName("Should broadcast string alert to all subscribed clients")
    public void testBroadcastAlert() {
        notificationService.broadcastAlert("System maintenance in 5 minutes");

        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/alerts"),
            eq("System maintenance in 5 minutes")
        );
    }

    /**
     * Test 12: Verify broadcast alert DTO sends to all clients.
     */
    @Test
    @DisplayName("Should broadcast alert DTO to all subscribed clients")
    public void testBroadcastAlertDTO() {
        InventoryAlertDTO alert = InventoryAlertDTO.builder()
            .alertType("RESTOCK_RECEIVED")
            .message("New stock arrived")
            .build();

        notificationService.broadcastAlert(alert);

        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/alerts"),
            eq(alert)
        );
    }

    /**
     * Test 13: Verify check and notify all low-stock items.
     */
    @Test
    @DisplayName("Should check and notify all low-stock items individually")
    public void testCheckAndNotifyLowStock() {
        List<InventoryItem> lowStockItems = new ArrayList<>(List.of(lowStockItem));
        when(inventoryItemRepository.findBelowMinimumStockLevel())
            .thenReturn(lowStockItems);

        List<InventoryItem> result = notificationService.checkAndNotifyLowStock();

        assertThat(result).hasSize(1);
        // Verify notification sent for each item
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/alerts"),
            org.mockito.ArgumentMatchers.isA(InventoryAlertDTO.class)
        );
    }

    /**
     * Test 14: Verify check and notify all out-of-stock items.
     */
    @Test
    @DisplayName("Should check and notify all out-of-stock items individually")
    public void testCheckAndNotifyOutOfStock() {
        List<InventoryItem> outOfStockItems = new ArrayList<>(List.of(outOfStockItem));
        when(inventoryItemRepository.findOutOfStock())
            .thenReturn(outOfStockItems);

        List<InventoryItem> result = notificationService.checkAndNotifyOutOfStock();

        assertThat(result).hasSize(1);
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/alerts"),
            org.mockito.ArgumentMatchers.isA(InventoryAlertDTO.class)
        );
    }

    /**
     * Test 15: Verify inventory alert DTO contains all required fields.
     */
    @Test
    @DisplayName("Should create inventory alert DTO with all required fields")
    public void testInventoryAlertDTOFields() {
        InventoryAlertDTO alert = InventoryAlertDTO.builder()
            .timestamp(LocalDateTime.now())
            .storeId(1L)
            .productId(100L)
            .quantity(25)
            .minimumLevel(10)
            .alertType("LOW_STOCK")
            .message("Low stock alert")
            .critical(false)
            .priority("HIGH")
            .suggestedAction("REORDER")
            .build();

        assertThat(alert.getStoreId()).isEqualTo(1L);
        assertThat(alert.getProductId()).isEqualTo(100L);
        assertThat(alert.getQuantity()).isEqualTo(25);
        assertThat(alert.getMinimumLevel()).isEqualTo(10);
        assertThat(alert.getAlertType()).isEqualTo("LOW_STOCK");
        assertThat(alert.getMessage()).isEqualTo("Low stock alert");
        assertThat(alert.getCritical()).isFalse();
        assertThat(alert.getPriority()).isEqualTo("HIGH");
        assertThat(alert.getSuggestedAction()).isEqualTo("REORDER");
    }
}
