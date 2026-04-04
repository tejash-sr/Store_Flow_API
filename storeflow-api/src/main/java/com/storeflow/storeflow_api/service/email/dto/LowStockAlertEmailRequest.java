package com.storeflow.storeflow_api.service.email.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for sending low-stock alert email.
 * 
 * Contains product details, current quantity, threshold, and location.
 */
@Data
@Builder
public class LowStockAlertEmailRequest {
    private String toEmail;
    private String productName;
    private Long currentQty;
    private Long minimumLevel;
    private String warehouseLocation;
}
