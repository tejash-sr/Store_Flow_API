package com.storeflow.storeflow_api.dto;

import com.storeflow.storeflow_api.repository.CategoryRepository;
import com.storeflow.storeflow_api.validation.ExistsInDatabase;
import jakarta.validation.constraints.*;
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
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 150, message = "Product name must be between 3 and 150 characters")
    private String name;

    @Size(max = 3000, message = "Description cannot exceed 3000 characters")
    private String description;

    @NotBlank(message = "SKU is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Long stockQuantity;

    @NotNull(message = "Category ID is required")
    @ExistsInDatabase(repositoryClass = CategoryRepository.class, message = "Category with this ID does not exist")
    private Long categoryId;

    private String imageUrl;
}
