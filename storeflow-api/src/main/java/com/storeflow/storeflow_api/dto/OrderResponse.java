package com.storeflow.storeflow_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.storeflow.storeflow_api.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for order data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String referenceNumber;
    private Long customerId;
    @JsonIgnore
    private User customer;
    private String customerName;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
