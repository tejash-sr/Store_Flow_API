package com.storeflow.storeflow_api.graphql;

import com.storeflow.storeflow_api.entity.Order;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.service.OrderService;
import com.storeflow.storeflow_api.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

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
    public Product createProduct(@Argument Map<String, Object> input) {
        log.info("GraphQL: Creating product with input: {}", input);
        
        String name = (String) input.get("name");
        String description = (String) input.get("description");
        String sku = (String) input.get("sku");
        Double price = ((Number) input.get("price")).doubleValue();
        Integer stockQuantity = ((Number) input.get("stockQuantity")).intValue();
        Long categoryId = Long.parseLong(input.get("categoryId").toString());
        String imageUrl = (String) input.get("imageUrl");
        
        // TODO: Use ProductService.createProduct with proper DTO
        return null; // Placeholder
    }
    
    /**
     * Mutation: updateProduct - Update existing product (admin only)
     */
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Product updateProduct(
            @Argument Long id,
            @Argument Map<String, Object> input
    ) {
        log.info("GraphQL: Updating product {} with input: {}", id, input);
        
        // TODO: Use ProductService.updateProduct with proper DTO
        return productService.getProductById(id);
    }
    
    /**
     * Mutation: adjustStock - Adjust product stock levels (admin only)
     */
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Product adjustStock(
            @Argument Long productId,
            @Argument Integer quantity
    ) {
        log.info("GraphQL: Adjusting stock for product {} by {}", productId, quantity);
        
        Product product = productService.getProductById(productId);
        product.setStockQuantity(product.getStockQuantity() + quantity);
        
        // TODO: Use ProductService.updateProduct
        return product;
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
    public Order placeOrder(@Argument Map<String, Object> input) {
        log.info("GraphQL: Placing order with input: {}", input);
        
        // TODO: Extract items and address from input
        // Parse items list
        // Parse shipping address
        // Use OrderService.createOrder
        
        return null; // Placeholder
    }
    
    /**
     * Mutation: updateOrderStatus - Update order status (admin only)
     */
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Order updateOrderStatus(
            @Argument Long orderId,
            @Argument String status
    ) {
        log.info("GraphQL: Updating order {} status to {}", orderId, status);
        
        Order order = orderService.getOrderById(orderId);
        // TODO: Use OrderService.updateOrderStatus
        return order;
    }
}
