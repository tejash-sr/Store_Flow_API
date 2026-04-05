package com.storeflow.storeflow_api.dto;

import com.storeflow.storeflow_api.entity.Category;
import com.storeflow.storeflow_api.entity.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for product data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private Long stockQuantity;
    private String categoryName;
    private Long categoryId;
    private Category category;
    private String imageUrl;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
