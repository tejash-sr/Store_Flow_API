package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.dto.InventoryAlertDTO;
import com.storeflow.storeflow_api.entity.InventoryItem;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.entity.Store;
import com.storeflow.storeflow_api.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Service for sending real-time inventory alert notifications.
 * 
 * Provides functionality to:
 * - Monitor and notify low-stock conditions
 * - Send out-of-stock alerts
 * - Broadcast inventory changes via WebSocket
 * - Track alert history and frequency
 * 
 * Uses Spring's messaging template to send STOMP messages to connected WebSocket clients
 * via the /topic/alerts and /topic/inventory destinations.
 * 
 * @author StoreFlow
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final InventoryItemRepository inventoryItemRepository;

    /**
     * Notify all connected clients about low-stock inventory items.
     * 
     * Broadcasts a list of inventory items that are below their minimum stock level
     * to all WebSocket clients subscribed to /topic/alerts.
     * 
     * Useful for dashboard monitoring and alerting store managers.
     * 
     * @return list of low-stock inventory items
     */
    public List<InventoryItem> notifyLowStockItems() {
        List<InventoryItem> lowStockItems = inventoryItemRepository.findBelowMinimumStockLevel();
        
        if (!lowStockItems.isEmpty()) {
            // Sort by urgency (lowest stock first)
            lowStockItems.sort(Comparator.comparing(InventoryItem::getQuantityOnHand));
            
            // Broadcast to all connected clients
            String message = String.format(
                "LOW_STOCK_ALERT: %d items below minimum stock level. Most critical: %s (qty: %d)",
                lowStockItems.size(),
                lowStockItems.get(0).getProduct().getName(),
                lowStockItems.get(0).getQuantityOnHand()
            );
            
            messagingTemplate.convertAndSend(
                "/topic/alerts",
                message
            );
            
            log.info("Low stock alert sent: {} items below minimum", lowStockItems.size());
        }
        
        return lowStockItems;
    }

    /**
     * Notify about out-of-stock products.
     * 
     * Broadcasts inventory items with zero quantity to alert managers
     * that products need immediate restocking.
     * 
     * @return list of out-of-stock inventory items
     */
    public List<InventoryItem> notifyOutOfStockItems() {
        List<InventoryItem> outOfStockItems = inventoryItemRepository.findOutOfStock();
        
        if (!outOfStockItems.isEmpty()) {
            String message = String.format(
                "OUT_OF_STOCK_ALERT: %d items have zero quantity and need immediate restocking",
                outOfStockItems.size()
            );
            
            messagingTemplate.convertAndSend(
                "/topic/alerts",
                message
            );
            
            log.warn("Out of stock alert sent: {} items with zero quantity", outOfStockItems.size());
        }
        
        return outOfStockItems;
    }

    /**
     * Send inventory update notification for a specific store.
     * 
     * Broadcasts inventory changes for a particular store location to all subscribed clients.
     * Used when inventory is updated to keep clients synchronized.
     * 
     * @param storeId the ID of the store with inventory changes
     * @param productId the ID of the product that was updated
     * @param newQuantity the new quantity after update
     */
    public void notifyInventoryUpdate(Long storeId, Long productId, Long newQuantity) {
        InventoryAlertDTO alert = InventoryAlertDTO.builder()
            .timestamp(LocalDateTime.now())
            .storeId(storeId)
            .productId(productId)
            .quantity(newQuantity.intValue())
            .alertType("INVENTORY_UPDATE")
            .message(String.format("Inventory updated - Store %d, Product %d: %d units", 
                storeId, productId, newQuantity))
            .build();
        
        messagingTemplate.convertAndSend(
            "/topic/inventory",
            alert
        );
        
        log.info("Inventory update notification sent for store {}, product {}", storeId, productId);
    }

    /**
     * Send low-stock alert for a specific inventory item.
     * 
     * Broadcasts a detailed alert about a specific store's low inventory
     * to all subscribed clients.
     * 
     * @param inventoryItem the low-stock inventory item
     */
    public void notifyLowStockForItem(InventoryItem inventoryItem) {
        if (inventoryItem == null) {
            log.warn("Cannot notify: inventory item is null");
            return;
        }
        
        Product product = inventoryItem.getProduct();
        Store store = inventoryItem.getStore();
        
        InventoryAlertDTO alert = InventoryAlertDTO.builder()
            .timestamp(LocalDateTime.now())
            .storeId(store.getId())
            .productId(product.getId())
            .quantity(inventoryItem.getQuantityOnHand().intValue())
            .minimumLevel(inventoryItem.getMinimumStockLevel().intValue())
            .alertType("LOW_STOCK")
            .message(String.format(
                "LOW_STOCK: %s at %s - Current: %d units, Minimum: %d units",
                product.getName(),
                store.getName(),
                inventoryItem.getQuantityOnHand(),
                inventoryItem.getMinimumStockLevel()
            ))
            .build();
        
        messagingTemplate.convertAndSend(
            "/topic/alerts",
            alert
        );
        
        log.warn("Low stock alert sent: {} at {} (qty: {})",
            product.getName(), store.getName(), inventoryItem.getQuantityOnHand());
    }

    /**
     * Send out-of-stock alert for a specific inventory item.
     * 
     * Broadcasts a critical alert about a product that is out of stock
     * at a specific store location.
     * 
     * @param inventoryItem the out-of-stock inventory item
     */
    public void notifyOutOfStockForItem(InventoryItem inventoryItem) {
        if (inventoryItem == null) {
            log.warn("Cannot notify: inventory item is null");
            return;
        }
        
        Product product = inventoryItem.getProduct();
        Store store = inventoryItem.getStore();
        
        InventoryAlertDTO alert = InventoryAlertDTO.builder()
            .timestamp(LocalDateTime.now())
            .storeId(store.getId())
            .productId(product.getId())
            .quantity(0)
            .minimumLevel(inventoryItem.getMinimumStockLevel().intValue())
            .alertType("OUT_OF_STOCK")
            .message(String.format(
                "OUT_OF_STOCK: %s at %s - Urgent restocking required!",
                product.getName(),
                store.getName()
            ))
            .critical(true)
            .build();
        
        messagingTemplate.convertAndSend(
            "/topic/alerts",
            alert
        );
        
        log.error("OUT_OF_STOCK ALERT: {} at {} - URGENT RESTOCKING REQUIRED",
            product.getName(), store.getName());
    }

    /**
     * Send a general inventory notification to specific users via /queue.
     * 
     * Sends a user-specific message (not broadcast) to a particular user
     * by their username. Used for individual notifications and alerts.
     * 
     * @param username the username of the recipient
     * @param message the notification message
     */
    public void notifyUser(String username, String message) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/notifications",
            message
        );
        
        log.info("User notification sent to: {}", username);
    }

    /**
     * Send a structured alert notification to a specific user.
     * 
     * Sends a detailed alert DTO to a particular user's private queue.
     * 
     * @param username the username of the recipient
     * @param alert the alert details
     */
    public void notifyUserAlert(String username, InventoryAlertDTO alert) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/notifications",
            alert
        );
        
        log.info("Alert notification sent to user: {}, alertType: {}",
            username, alert.getAlertType());
    }

    /**
     * Broadcast a general notification to all connected clients.
     * 
     * Sends a message to all subscribed clients on the /topic/alerts destination.
     * 
     * @param message the message to broadcast
     */
    public void broadcastAlert(String message) {
        messagingTemplate.convertAndSend(
            "/topic/alerts",
            message
        );
        
        log.info("Broadcast alert sent: {}", message);
    }

    /**
     * Broadcast a structured alert to all connected clients.
     * 
     * @param alert the alert DTO to broadcast
     */
    public void broadcastAlert(InventoryAlertDTO alert) {
        messagingTemplate.convertAndSend(
            "/topic/alerts",
            alert
        );
        
        log.info("Broadcast alert sent: alertType={}, message={}",
            alert.getAlertType(), alert.getMessage());
    }

    /**
     * Check and notify all low-stock conditions.
     * 
     * Performs a full scan of inventory and notifies about all items
     * below minimum stock levels. Should be called periodically (e.g., via @Scheduled).
     * 
     * @return list of all low-stock items that were notified
     */
    public List<InventoryItem> checkAndNotifyLowStock() {
        List<InventoryItem> lowStockItems = inventoryItemRepository.findBelowMinimumStockLevel();
        
        for (InventoryItem item : lowStockItems) {
            notifyLowStockForItem(item);
        }
        
        log.info("Low stock check completed: {} items notified", lowStockItems.size());
        return lowStockItems;
    }

    /**
     * Check and notify all out-of-stock conditions.
     * 
     * Performs a full scan of inventory and notifies about all items
     * with zero quantity. Should be called periodically (e.g., via @Scheduled).
     * 
     * @return list of all out-of-stock items that were notified
     */
    public List<InventoryItem> checkAndNotifyOutOfStock() {
        List<InventoryItem> outOfStockItems = inventoryItemRepository.findOutOfStock();
        
        for (InventoryItem item : outOfStockItems) {
            notifyOutOfStockForItem(item);
        }
        
        log.info("Out of stock check completed: {} items notified", outOfStockItems.size());
        return outOfStockItems;
    }
}
