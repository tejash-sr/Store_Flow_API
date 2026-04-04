package com.storeflow.storeflow_api.controller;

import com.storeflow.storeflow_api.dto.OrderRequest;
import com.storeflow.storeflow_api.dto.OrderResponse;
import com.storeflow.storeflow_api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for Order endpoints.
 * Implements order placement, listing, and status management.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders - Place a new order (atomic transaction).
     */
    @PostMapping
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderRequest request) {
        try {
            OrderResponse response = orderService.placeOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{ \"error\": \"" + e.getMessage() + "\" }");
        }
    }

    /**
     * GET /api/orders - List all orders with pagination (USER: own, ADMIN: all).
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(Pageable pageable) {
        Page<OrderResponse> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/{id} - Get order details with all order items.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        Optional<OrderResponse> order = orderService.getOrderById(id);
        if (order.isPresent()) {
            return ResponseEntity.ok(order.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("{ \"error\": \"Order not found\" }");
    }

    /**
     * PATCH /api/orders/{id}/status - Update order status (admin, valid transitions only).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            OrderResponse response = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{ \"error\": \"" + e.getMessage() + "\" }");
        }
    }
}
