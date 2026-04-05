package com.storeflow.storeflow_api.dto;

import com.storeflow.storeflow_api.entity.Category;
import com.storeflow.storeflow_api.entity.enums.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Product response payload with all product details")
public class ProductResponse {

    @Schema(description = "Unique product identifier", example = "1")
    private Long id;
    
    @Schema(description = "Product name", example = "Laptop Dell XPS 13")
    private String name;
    
    @Schema(description = "Product description", example = "High-performance laptop")
    private String description;
    
    @Schema(description = "Stock Keeping Unit", example = "SKU-DELL-XPS-001")
    private String sku;
    
    @Schema(description = "Product price", example = "999.99")
    private BigDecimal price;
    
    @Schema(description = "Quantity in stock", example = "50")
    private Long stockQuantity;
    
    @Schema(description = "Category name", example = "Electronics")
    private String categoryName;
    
    @Schema(description = "Category ID", example = "1")
    private Long categoryId;
    
    @Schema(description = "Category object with full details")
    private Category category;
    
    @Schema(description = "Product image URL", example = "https://example.com/images/product.jpg")
    private String imageUrl;
    
    @Schema(description = "Product status (ACTIVE/INACTIVE/DISCONTINUED)", example = "ACTIVE")
    private ProductStatus status;
    
    @Schema(description = "Timestamp when product was created", example = "2026-04-05T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp of last update", example = "2026-04-05T12:45:00")
    private LocalDateTime updatedAt;
}
