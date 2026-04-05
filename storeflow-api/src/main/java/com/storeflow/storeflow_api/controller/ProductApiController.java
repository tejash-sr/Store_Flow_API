package com.storeflow.storeflow_api.controller;

import com.storeflow.storeflow_api.dto.request.CreateProductRequest;
import com.storeflow.storeflow_api.dto.response.ProductResponse;
import com.storeflow.storeflow_api.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Product Controller with comprehensive OpenAPI documentation
 * Every endpoint, parameter, and response is documented for Swagger UI
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(
        name = "Products",
        description = "Product management endpoints - CRUD operations, search, filtering"
)
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * Get all products with optional filters and pagination
     * Public endpoint - no authentication required
     */
    @GetMapping
    @Operation(
            summary = "List all products",
            description = "Retrieve paginated list of products with optional filtering by name, category, price range, status",
            operationId = "getProducts",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved products",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Products List Example",
                                    value = "{\n" +
                                            "  \"content\": [\n" +
                                            "    {\n" +
                                            "      \"id\": 1,\n" +
                                            "      \"name\": \"Laptop\",\n" +
                                            "      \"sku\": \"LAPTOP-001\",\n" +
                                            "      \"price\": 1299.99,\n" +
                                            "      \"stockQuantity\": 50,\n" +
                                            "      \"status\": \"ACTIVE\"\n" +
                                            "    }\n" +
                                            "  ],\n" +
                                            "  \"page\": 0,\n" +
                                            "  \"size\": 20,\n" +
                                            "  \"totalElements\": 100,\n" +
                                            "  \"totalPages\": 5,\n" +
                                            "  \"first\": true,\n" +
                                            "  \"last\": false,\n" +
                                            "  \"hasNext\": true\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination or filter parameters"
            )
    })
    public ResponseEntity<?> getProducts(
            @Parameter(
                    name = "page",
                    description = "Page number (0-based)",
                    in = ParameterIn.QUERY,
                    example = "0"
            )
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(
                    name = "size",
                    description = "Items per page (max 100)",
                    in = ParameterIn.QUERY,
                    example = "20"
            )
            @RequestParam(defaultValue = "20") Integer size,
            
            @Parameter(
                    name = "name",
                    description = "Filter by product name (partial match, case-insensitive)",
                    in = ParameterIn.QUERY,
                    example = "Laptop"
            )
            @RequestParam(required = false) String name,
            
            @Parameter(
                    name = "minPrice",
                    description = "Minimum price filter",
                    in = ParameterIn.QUERY,
                    example = "100.00"
            )
            @RequestParam(required = false) Double minPrice,
            
            @Parameter(
                    name = "maxPrice",
                    description = "Maximum price filter",
                    in = ParameterIn.QUERY,
                    example = "5000.00"
            )
            @RequestParam(required = false) Double maxPrice
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        // TODO: Implement filtering
        Page<?> products = Page.empty();
        return ResponseEntity.ok(products);
    }
    
    /**
     * Create new product (Admin only)
     * Requires Bearer token with ADMIN role
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create new product",
            description = "Create a new product with complete details. Requires ADMIN role.",
            operationId = "createProduct",
            tags = {"Products"},
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Product created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body - validation failed"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - missing or invalid Bearer token"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user does not have ADMIN role"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - SKU already exists"
            )
    })
    public ResponseEntity<?> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product creation payload",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateProductRequest.class),
                            examples = @ExampleObject(
                                    name = "Create Product Example",
                                    description = "Example of creating a wireless headphones product",
                                    value = "{\n" +
                                            "  \"name\": \"Wireless Headphones\",\n" +
                                            "  \"description\": \"Premium with noise cancellation\",\n" +
                                            "  \"sku\": \"WH-001\",\n" +
                                            "  \"price\": 299.99,\n" +
                                            "  \"stockQuantity\": 50,\n" +
                                            "  \"categoryId\": 1\n" +
                                            "}"
                            )
                    )
            )
            @Valid @RequestBody CreateProductRequest request
    ) {
        // TODO: Implement product creation
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    /**
     * Get single product by ID
     * Public endpoint
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by ID",
            description = "Retrieve detailed information for a specific product",
            operationId = "getProductById",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            )
    })
    public ResponseEntity<?> getProductById(
            @Parameter(
                    name = "id",
                    description = "Product ID",
                    example = "1",
                    required = true
            )
            @PathVariable Long id
    ) {
        // TODO: Implement get by ID
        return ResponseEntity.ok().build();
    }
}
