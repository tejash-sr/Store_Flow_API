package com.storeflow.storeflow_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CreateProductRequest DTO
 * Documented with OpenAPI/Swagger annotations for interactive API documentation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "CreateProductRequest",
        description = "Request payload for creating a new product (admin only)",
        example = "{\n" +
                "  \"name\": \"Wireless Headphones\",\n" +
                "  \"description\": \"Premium noise-cancelling wireless headphones\",\n" +
                "  \"sku\": \"WH-PREMIUM-001\",\n" +
                "  \"price\": 299.99,\n" +
                "  \"stockQuantity\": 50,\n" +
                "  \"categoryId\": 1,\n" +
                "  \"imageUrl\": \"https://example.com/images/headphones.jpg\"\n" +
                "}"
)
public class CreateProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 150, message = "Product name must be between 3 and 150 characters")
    @Schema(
            description = "Product name (unique within category)",
            example = "Wireless Headphones",
            maxLength = 150,
            minLength = 3
    )
    private String name;
    
    @Size(max = 3000, message = "Description must not exceed 3000 characters")
    @Schema(
            description = "Detailed product description",
            example = "Premium noise-cancelling wireless headphones with 30-hour battery life",
            maxLength = 3000
    )
    private String description;
    
    @NotBlank(message = "SKU is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    @Schema(
            description = "Stock Keeping Unit - unique product identifier (uppercase alphanumeric)",
            example = "WH-PREMIUM-001",
            pattern = "^[A-Z0-9-]+$"
    )
    private String sku;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Schema(
            description = "Product price in USD",
            example = "299.99",
            type = "number",
            format = "decimal",
            minimum = "0.01"
    )
    private Double price;
    
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Schema(
            description = "Available quantity in stock",
            example = "50",
            type = "integer",
            minimum = "0"
    )
    private Integer stockQuantity;
    
    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be a positive number")
    @Schema(
            description = "ID of the product category (must exist)",
            example = "1",
            type = "integer",
            maximum = "999999"
    )
    private Long categoryId;
    
    @Schema(
            description = "Product image URL (optional)",
            example = "https://example.com/images/headphones.jpg",
            nullable = true
    )
    private String imageUrl;
}
