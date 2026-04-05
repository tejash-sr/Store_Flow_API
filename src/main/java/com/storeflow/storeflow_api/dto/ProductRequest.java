package com.storeflow.storeflow_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating and updating products.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating or updating a product")
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 150, message = "Product name must be between 3 and 150 characters")
    @Schema(description = "Product name (3-150 characters)", example = "Laptop Dell XPS 13")
    private String name;

    @Size(max = 3000, message = "Description cannot exceed 3000 characters")
    @Schema(description = "Product description (max 3000 characters)", example = "High-performance laptop with 13-inch display")
    private String description;

    @NotBlank(message = "SKU is required")
    @Size(min = 1, max = 100, message = "SKU must be between 1 and 100 characters")
    @Schema(description = "Stock Keeping Unit - unique product identifier", example = "SKU-DELL-XPS-001")
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Schema(description = "Product price (must be > 0)", example = "999.99")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Schema(description = "Quantity in stock (non-negative)", example = "50")
    private Long stockQuantity;

    @NotNull(message = "Category ID is required")
    @Schema(description = "Category ID for product classification", example = "1")
    private Long categoryId;

    @Schema(description = "Product image URL", example = "https://example.com/images/product.jpg")
    private String imageUrl;
}
