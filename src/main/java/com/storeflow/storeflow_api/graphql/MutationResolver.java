package com.storeflow.storeflow_api.graphql;

import com.storeflow.storeflow_api.dto.OrderItemRequest;
import com.storeflow.storeflow_api.dto.OrderRequest;
import com.storeflow.storeflow_api.dto.OrderResponse;
import com.storeflow.storeflow_api.dto.ProductRequest;
import com.storeflow.storeflow_api.dto.ProductResponse;
import com.storeflow.storeflow_api.service.OrderService;
import com.storeflow.storeflow_api.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GraphQL Mutation Resolver
 * Handles all mutation operations: create, update, delete
 */
@Slf4j
@Controller
public class MutationResolver {
    
    private final ProductService productService;
    private final OrderService orderService;
    
    public MutationResolver(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }
    
    /**
     * Mutation: createProduct - Create new product (admin only)
     */
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
        public ProductResponse createProduct(@Argument Map<String, Object> input) {
        log.info("GraphQL: Creating product with input: {}", input);
        
        ProductRequest request = ProductRequest.builder()
            .name((String) input.get("name"))
            .description((String) input.get("description"))
            .sku((String) input.get("sku"))
            .price(new BigDecimal(input.get("price").toString()))
            .stockQuantity(Long.valueOf(input.get("stockQuantity").toString()))
            .categoryId(Long.valueOf(input.get("categoryId").toString()))
            .imageUrl((String) input.get("imageUrl"))
            .build();

        return productService.createProduct(request);
    }
    
    /**
     * Mutation: updateProduct - Update existing product (admin only)
     */
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
        public ProductResponse updateProduct(
            @Argument Long id,
            @Argument Map<String, Object> input
    ) {
        log.info("GraphQL: Updating product {} with input: {}", id, input);
        
        ProductResponse currentProduct = productService.getProductById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        ProductRequest request = ProductRequest.builder()
            .name(input.containsKey("name") ? (String) input.get("name") : currentProduct.getName())
            .description(input.containsKey("description") ? (String) input.get("description") : currentProduct.getDescription())
            .sku(input.containsKey("sku") ? (String) input.get("sku") : currentProduct.getSku())
            .price(input.containsKey("price") && input.get("price") != null
                ? new BigDecimal(input.get("price").toString())
                : currentProduct.getPrice())
            .stockQuantity(input.containsKey("stockQuantity") && input.get("stockQuantity") != null
                ? Long.valueOf(input.get("stockQuantity").toString())
                : currentProduct.getStockQuantity())
            .categoryId(input.containsKey("categoryId") && input.get("categoryId") != null
                ? Long.valueOf(input.get("categoryId").toString())
                : currentProduct.getCategoryId())
            .imageUrl(input.containsKey("imageUrl") ? (String) input.get("imageUrl") : currentProduct.getImageUrl())
            .build();

        return productService.updateProduct(id, request);
    }
    
    /**
     * Mutation: adjustStock - Adjust product stock levels (admin only)
     */
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
        public ProductResponse adjustStock(
            @Argument Long productId,
            @Argument Integer quantity
    ) {
        log.info("GraphQL: Adjusting stock for product {} by {}", productId, quantity);
        
        return productService.adjustStock(productId, quantity.longValue());
    }
    
    /**
     * Mutation: deleteProduct - Delete product (admin only)
     */
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteProduct(@Argument Long id) {
        log.info("GraphQL: Deleting product {}", id);
        
        try {
            productService.deleteProduct(id);
            return true;
        } catch (Exception e) {
            log.error("Error deleting product: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Mutation: placeOrder - Create new order (authenticated users)
     */
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public OrderResponse placeOrder(@Argument Map<String, Object> input) {
        log.info("GraphQL: Placing order with input: {}", input);
        
        List<OrderItemRequest> items = new ArrayList<>();
        Object itemsObject = input.get("items");
        if (itemsObject instanceof List<?> rawItems) {
            for (Object rawItem : rawItems) {
                if (rawItem instanceof Map<?, ?> itemMap) {
                    items.add(OrderItemRequest.builder()
                            .productId(Long.valueOf(itemMap.get("productId").toString()))
                            .quantity(Long.valueOf(itemMap.get("quantity").toString()))
                            .build());
                }
            }
        }

        OrderRequest request = OrderRequest.builder()
                .customerId(0L)
                .items(items)
                .build();

        OrderResponse response = orderService.placeOrder(request);
        response.setCustomerId(0L);

        Object shippingAddress = input.get("shippingAddress");
        if (shippingAddress != null) {
            response.setShippingAddress(shippingAddress.toString());
        }

        return response;
    }
    
    /**
     * Mutation: updateOrderStatus - Update order status (admin only)
     */
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse updateOrderStatus(
            @Argument Long orderId,
            @Argument String status
    ) {
        log.info("GraphQL: Updating order {} status to {}", orderId, status);
        
        return orderService.updateOrderStatus(orderId, status);
    }
}
