package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.dto.ProductRequest;
import com.storeflow.storeflow_api.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service interface for product operations.
 */
public interface ProductService {

    /**
     * Create a new product.
     */
    ProductResponse createProduct(ProductRequest request);

    /**
     * Get all products with pagination and filtering.
     */
    Page<ProductResponse> getAllProducts(Pageable pageable, String name, String status);

    /**
     * Get a product by ID.
     */
    Optional<ProductResponse> getProductById(Long id);

    /**
     * Update a product.
     */
    ProductResponse updateProduct(Long id, ProductRequest request);

    /**
     * Adjust product stock.
     */
    ProductResponse adjustStock(Long id, Long quantityChange);

    /**
     * Soft delete a product (mark as DISCONTINUED).
     */
    void deleteProduct(Long id);
}
