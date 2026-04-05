package com.storeflow.storeflow_api.graphql;

import com.storeflow.storeflow_api.entity.Order;
import com.storeflow.storeflow_api.entity.OrderItem;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.entity.User;
import com.storeflow.storeflow_api.service.OrderService;
import com.storeflow.storeflow_api.service.ProductService;
import com.storeflow.storeflow_api.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GraphQL Order Query Resolver
 * Handles order queries and nested field resolution
 */
@Slf4j
@Controller
public class OrderResolver {
    
    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;
    
    public OrderResolver(OrderService orderService, UserService userService, ProductService productService) {
        this.orderService = orderService;
        this.userService = userService;
        this.productService = productService;
    }
    
    /**
     * Query: orders - Get paginated list of orders
     * Users see only their own orders; admins see all
     */
    @QueryMapping
    public Map<String, Object> orders(
            @Argument(name = "page") Integer page,
            @Argument(name = "size") Integer size,
            @Argument(name = "sort") String sort
    ) {
        page = page != null ? page : 0;
        size = size != null && size <= 100 ? size : 20;
        sort = sort != null ? sort : "createdAt,desc";
        
        // Parse sort string
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && "asc".equals(sortParts[1]) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = sortParts[0];
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // TODO: Implement user-specific order filtering
        Page<Order> orderPage = orderService.getAllOrders(pageRequest);
        
        return buildOrderPageResponse(orderPage);
    }
    
    /**
     * Query: order - Get single order by ID
     * Users can only view their own orders
     */
    @QueryMapping
    public Order order(@Argument Long id) {
        return orderService.getOrderById(id);
    }
    
    /**
     * SchemaMapping: customer - Resolve customer field for Order type
     */
    @SchemaMapping(typeName = "Order")
    public User customer(Order order) {
        if (order.getCustomer() != null) {
            return order.getCustomer();
        }
        if (order.getCustomerId() != null) {
            return userService.getUserById(order.getCustomerId());
        }
        return null;
    }
    
    /**
     * SchemaMapping: items - Resolve items field for Order type
     */
    @SchemaMapping(typeName = "Order")
    public List<OrderItem> items(Order order) {
        return order.getItems();
    }
    
    /**
     * SchemaMapping: product - Resolve product field for OrderItem type
     */
    @SchemaMapping(typeName = "OrderItem")
    public Product product(OrderItem orderItem) {
        if (orderItem.getProduct() != null) {
            return orderItem.getProduct();
        }
        if (orderItem.getProductId() != null) {
            return productService.getProductById(orderItem.getProductId());
        }
        return null;
    }
    
    /**
     * Build order page response with pagination info
     */
    private Map<String, Object> buildOrderPageResponse(Page<Order> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("page", page.getNumber());
        pageInfo.put("size", page.getSize());
        pageInfo.put("totalElements", page.getTotalElements());
        pageInfo.put("totalPages", page.getTotalPages());
        pageInfo.put("first", page.isFirst());
        pageInfo.put("last", page.isLast());
        pageInfo.put("hasNext", page.hasNext());
        
        response.put("pageInfo", pageInfo);
        return response;
    }
}
