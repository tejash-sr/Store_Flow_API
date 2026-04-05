package com.storeflow.storeflow_api.controller;

import com.storeflow.storeflow_api.dto.ProductRequest;
import com.storeflow.storeflow_api.dto.ProductResponse;
import com.storeflow.storeflow_api.dto.FileUploadResponse;
import com.storeflow.storeflow_api.dto.PageResponse;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.specification.ProductSpecification;
import com.storeflow.storeflow_api.service.ProductService;
import com.storeflow.storeflow_api.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
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
    public ResponseEntity<FileUploadResponse> uploadProductImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            // Verify product exists
            Optional<ProductResponse> product = productService.getProductById(id);
            if (product.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(FileUploadResponse.builder()
                        .error("Product not found")
                        .build());
            }

            // Save file
            String filePath = fileStorageService.saveProductImage(file, id.toString());
            log.info("Product image uploaded for product {}: {}", id, filePath);

            return ResponseEntity.ok(FileUploadResponse.builder()
                .message("Image uploaded successfully")
                .filePath(filePath)
                .build());

        } catch (IllegalArgumentException e) {
            log.warn("Invalid file for product {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(FileUploadResponse.builder()
                    .error(e.getMessage())
                    .build());
        } catch (IOException e) {
            log.error("Failed to upload image for product {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FileUploadResponse.builder()
                    .error("Failed to upload image")
                    .build());
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
                    .body(FileUploadResponse.builder()
                        .error("Product not found")
                        .build());
            }

            // Try to load the most recent image for this product
            String filePath = "products/" + id + "/image.jpg";
            byte[] imageBytes = fileStorageService.loadFile(filePath);

            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"product-" + id + ".jpg\"")
                .body(new ByteArrayResource(imageBytes));

        } catch (IOException e) {
            log.warn("Image not found for product {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(FileUploadResponse.builder()
                    .error("Image not found")
                    .build());
        }
    }

    /**
     * GET /api/products/search - Advanced product search with filters and specifications.
     * 
     * Supports filtering by:
     * - name: Product name (case-insensitive substring match)
     * - categoryId: Category ID (exact match)
     * - minPrice: Minimum price (inclusive)
     * - maxPrice: Maximum price (inclusive)
     * - isActive: Active status (true/false)
     * 
     * @param page page number (0-indexed), default 0
     * @param size page size, default 20
     * @param name product name filter (optional)
     * @param categoryId category ID filter (optional)
     * @param minPrice minimum price filter (optional)
     * @param maxPrice maximum price filter (optional)
     * @param isActive active status filter (optional)
     * @return PageResponse containing matching products
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProductResponse>> advancedSearch(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isActive) {

        // Build specification from filter parameters
        Specification<Product> spec = Specification.where(null);

        if (name != null && !name.isBlank()) {
            spec = spec.and(ProductSpecification.nameContains(name));
        }
        if (categoryId != null) {
            spec = spec.and(ProductSpecification.categoryIdEquals(categoryId));
        }
        if (minPrice != null || maxPrice != null) {
            spec = spec.and(ProductSpecification.priceInRange(minPrice, maxPrice));
        }
        if (isActive != null) {
            spec = spec.and(ProductSpecification.isActiveEquals(isActive));
        }

        // Execute search with pagination
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> results = productService.searchProducts(spec, pageable);

        // Convert to PageResponse format
        PageResponse<ProductResponse> response = PageResponse.from(results);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/products/category/{categoryId} - Get all products in a category with pagination.
     * 
     * @param categoryId the category ID
     * @param page page number (0-indexed), default 0
     * @param size page size, default 20
     * @return PageResponse containing products in the specified category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Specification<Product> spec = ProductSpecification.categoryIdEquals(categoryId);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> results = productService.searchProducts(spec, pageable);

        return ResponseEntity.ok(PageResponse.from(results));
    }

    /**
     * GET /api/products/price-range - Filter products by price range with pagination.
     * 
     * @param minPrice minimum price (inclusive, optional)
     * @param maxPrice maximum price (inclusive, optional)
     * @param page page number (0-indexed), default 0
     * @param size page size, default 20
     * @return PageResponse containing products within price range
     */
    @GetMapping("/price-range")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByPriceRange(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (minPrice == null && maxPrice == null) {
            return ResponseEntity.badRequest().build();
        }

        Specification<Product> spec = ProductSpecification.priceInRange(minPrice, maxPrice);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> results = productService.searchProducts(spec, pageable);

        return ResponseEntity.ok(PageResponse.from(results));
    }

    /**
     * GET /api/products/active - Get all active products with pagination.
     * 
     * Convenience endpoint to retrieve only active/available products.
     * 
     * @param page page number (0-indexed), default 0
     * @param size page size, default 20
     * @return PageResponse containing all active products
     */
    @GetMapping("/active")
    public ResponseEntity<PageResponse<ProductResponse>> getActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Specification<Product> spec = ProductSpecification.isActive();
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> results = productService.searchProducts(spec, pageable);

        return ResponseEntity.ok(PageResponse.from(results));
    }
}
