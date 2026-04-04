package com.storeflow.storeflow_api.controller;

import com.storeflow.storeflow_api.dto.ProductRequest;
import com.storeflow.storeflow_api.dto.ProductResponse;
import com.storeflow.storeflow_api.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for Product endpoints.
 * Implements all CRUD operations for products.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * POST /api/products - Create a new product.
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/products - List all products with pagination and filtering.
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
        Pageable pageable,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String status) {
        Page<ProductResponse> products = productService.getAllProducts(pageable, name, status);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/{id} - Get a single product by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Optional<ProductResponse> product = productService.getProductById(id);
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("{ \"error\": \"Product not found\" }");
    }

    /**
     * PUT /api/products/{id} - Update a product (full update).
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse response = productService.updateProduct(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{ \"error\": \"" + e.getMessage() + "\" }");
        }
    }

    /**
     * PATCH /api/products/{id}/stock - Adjust product stock (atomic).
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> adjustStock(@PathVariable Long id, @RequestParam Long quantity) {
        try {
            ProductResponse response = productService.adjustStock(id, quantity);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{ \"error\": \"" + e.getMessage() + "\" }");
        }
    }

    /**
     * DELETE /api/products/{id} - Soft delete a product (mark as DISCONTINUED).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{ \"error\": \"" + e.getMessage() + "\" }");
        }
    }
}
