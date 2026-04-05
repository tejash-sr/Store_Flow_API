package com.storeflow.storeflow_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for inventory alert notifications sent via WebSocket.
 * 
 * Represents a structured alert about inventory changes, including
 * low-stock warnings, out-of-stock alerts, and general inventory updates.
 * 
 * Sent to WebSocket clients subscribed to /topic/alerts for real-time updates.
 * 
 * @author StoreFlow
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryAlertDTO {

    /**
     * Timestamp when the alert was generated.
     */
    private LocalDateTime timestamp;

    /**
     * The ID of the store affected by this alert.
     */
    private Long storeId;

    /**
     * The ID of the product affected by this alert.
     */
    private Long productId;

    /**
     * Current quantity in stock.
     */
    private Integer quantity;

    /**
     * Minimum stock level (warning threshold).
     * Optional - included when alert is triggered by low stock.
     */
    private Integer minimumLevel;

    /**
     * Type of alert: LOW_STOCK, OUT_OF_STOCK, INVENTORY_UPDATE, RESTOCK_RECEIVED, etc.
     */
    private String alertType;

    /**
     * Human-readable alert message.
     */
    private String message;

    /**
     * Whether this is a critical alert requiring immediate attention.
     * Default: false. Set true for out-of-stock alerts.
     */
    @Builder.Default
    private Boolean critical = false;

    /**
     * Priority level: HIGH, MEDIUM, LOW.
     * Optional - used for alert filtering and sorting.
     */
    private String priority;

    /**
     * Action suggested: REORDER, REDISTRIBUTE, INSPECT, NONE, etc.
     * Optional - guides staff on recommended response.
     */
    private String suggestedAction;
}
