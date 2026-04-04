package com.storeflow.storeflow_api.controller;

import com.storeflow.storeflow_api.dto.ProductRequest;
import com.storeflow.storeflow_api.dto.ProductResponse;
import com.storeflow.storeflow_api.service.ProductService;
import com.storeflow.storeflow_api.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

/**
 * REST Controller for Product endpoints.
 * Implements all CRUD operations for products.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;

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

    /**
     * POST /api/products/{id}/image - Upload product image.
     */
    @PostMapping("/{id}/image")
    public ResponseEntity<?> uploadProductImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            // Verify product exists
            Optional<ProductResponse> product = productService.getProductById(id);
            if (product.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{ \"error\": \"Product not found\" }");
            }

            // Save file
            String filePath = fileStorageService.saveProductImage(file, id.toString());
            log.info("Product image uploaded for product {}: {}", id, filePath);

            return ResponseEntity.ok()
                .body("{ \"message\": \"Image uploaded successfully\", \"filePath\": \"" + filePath + "\" }");

        } catch (IllegalArgumentException e) {
            log.warn("Invalid file for product {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{ \"error\": \"" + e.getMessage() + "\" }");
        } catch (IOException e) {
            log.error("Failed to upload image for product {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{ \"error\": \"Failed to upload image\" }");
        }
    }

    /**
     * GET /api/products/{id}/image - Download product image.
     */
    @GetMapping("/{id}/image")
    public ResponseEntity<?> downloadProductImage(@PathVariable Long id) {
        try {
            // Verify product exists
            Optional<ProductResponse> productOpt = productService.getProductById(id);
            if (productOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{ \"error\": \"Product not found\" }");
            }

            // Try to load the most recent image for this product
            // For now, return a placeholder; in production, you'd track which image is "current"
            String filePath = "products/" + id + "/image.jpg";
            byte[] imageBytes = fileStorageService.loadFile(filePath);

            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"product-" + id + ".jpg\"")
                .body(new ByteArrayResource(imageBytes));

        } catch (IOException e) {
            log.warn("Image not found for product {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{ \"error\": \"Image not found\" }");
        }
    }
}
