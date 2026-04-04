package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.dto.OrderRequest;
import com.storeflow.storeflow_api.dto.OrderResponse;
import com.storeflow.storeflow_api.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for order operations.
 */
public interface OrderService {

    /**
     * Place a new order (atomic: validate stock, deduct, create order).
     */
    OrderResponse placeOrder(OrderRequest request);

    /**
     * Get all orders (ADMIN: all, USER: own orders only).
     */
    Page<OrderResponse> getAllOrders(Pageable pageable);

    /**
     * Get an order by ID (as OrderResponse DTO).
     */
    Optional<OrderResponse> getOrderById(Long id);

    /**
     * Get an order by ID (as full Order entity for file generation).
     */
    Optional<Order> getOrderEntityById(Long id);

    /**
     * Get all orders for export (CSV).
     */
    List<Order> getAllOrdersForExport();

    /**
     * Update order status with valid transition check.
     */
    OrderResponse updateOrderStatus(Long id, String newStatus);
}
