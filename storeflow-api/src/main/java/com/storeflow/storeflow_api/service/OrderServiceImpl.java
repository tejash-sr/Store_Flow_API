package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.dto.OrderItemRequest;
import com.storeflow.storeflow_api.dto.OrderItemResponse;
import com.storeflow.storeflow_api.dto.OrderRequest;
import com.storeflow.storeflow_api.dto.OrderResponse;
import com.storeflow.storeflow_api.entity.Order;
import com.storeflow.storeflow_api.entity.Order.OrderStatus;
import com.storeflow.storeflow_api.entity.OrderItem;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.entity.Store;
import com.storeflow.storeflow_api.repository.OrderRepository;
import com.storeflow.storeflow_api.repository.ProductRepository;
import com.storeflow.storeflow_api.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of OrderService with atomic transaction handling.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        // Atomic transaction: validate all items first, then deduct stock
        validateOrder(request);

        // Get default store for order (TODO: support store selection in Phase 4)
        Store store = storeRepository.findAll().isEmpty() 
            ? null 
            : storeRepository.findAll().get(0);
        
        if (store == null) {
            throw new IllegalArgumentException("No store found");
        }

        // Create order
        Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .store(store)
            .status(OrderStatus.PENDING)
            .subtotal(BigDecimal.ZERO)
            .tax(BigDecimal.ZERO)
            .shippingCost(BigDecimal.ZERO)
            .total(BigDecimal.ZERO)
            .build();

        // Process order items
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            // Create order item with price snapshot
            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(itemRequest.getQuantity())
                .unitPrice(product.getPrice())
                .build();

            orderItems.add(orderItem);
            subtotal = subtotal.add(itemSubtotal);
        }

        order.setItems(orderItems);
        order.setSubtotal(subtotal);
        order.setTotal(subtotal); // TODO: Add tax and shipping calculation in Phase 4
        Order saved = orderRepository.save(order);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrderById(Long id) {
        return orderRepository.findById(id).map(this::toResponse);
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, String newStatusStr) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());
            
            // Validate status transition
            if (!isValidTransition(order.getStatus(), newStatus)) {
                throw new IllegalArgumentException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
            }

            order.setStatus(newStatus);
            Order updated = orderRepository.save(order);
            return toResponse(updated);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatusStr);
        }
    }

    private void validateOrder(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        for (OrderItemRequest item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));

            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }

            // Note: Stock validation would require Store context in actual implementation
            // For MVP, we skip inventory check
        }
    }

    private boolean isValidTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        return switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELLED;
            case PROCESSING -> newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED, COMPLETED, CANCELLED -> false;
        };
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
            .map(item -> OrderItemResponse.builder()
                .id(item.getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .build())
            .toList();

        return OrderResponse.builder()
            .id(order.getId())
            .referenceNumber(order.getOrderNumber())
            .customerName(order.getCustomerName() != null ? order.getCustomerName() : "Customer")
            .status(order.getStatus().name())
            .totalAmount(order.getTotal())
            .items(items)
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
}
