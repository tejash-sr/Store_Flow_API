package com.storeflow.storeflow_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.storeflow.storeflow_api.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for order line items.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long id;
    private Long orderId;
    private Long productId;
    @JsonIgnore
    private Product product;
    private String productName;
    private Long quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
