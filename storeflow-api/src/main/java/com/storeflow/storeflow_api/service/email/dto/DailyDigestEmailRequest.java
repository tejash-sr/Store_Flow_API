package com.storeflow.storeflow_api.service.email.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for sending daily digest email.
 * 
 * Contains order summary statistics for admin dashboard.
 */
@Data
@Builder
public class DailyDigestEmailRequest {
    private String toEmail;
    private int totalOrders;
    private String totalRevenue;
    private String avgOrderValue;
    private int pendingOrderCount;
}
