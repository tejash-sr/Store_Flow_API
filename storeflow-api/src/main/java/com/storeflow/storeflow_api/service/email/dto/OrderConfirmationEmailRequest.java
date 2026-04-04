package com.storeflow.storeflow_api.service.email.dto;

import com.storeflow.storeflow_api.service.email.EmailService;
import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Request DTO for sending order confirmation email.
 * 
 * Contains order details, customer info, items, and delivery address.
 */
@Data
@Builder
public class OrderConfirmationEmailRequest {
    private String toEmail;
    private String customerName;
    private String orderNumber;
    private List<EmailService.OrderItem> items;
    private String totalAmount;
    private String deliveryAddress;
}
