package com.storeflow.storeflow_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity representing customer orders in the system.
 * Manages order status, totals, and relationships to order items and stores.
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_number", columnList = "order_number", unique = true),
    @Index(name = "idx_order_store_id", columnList = "store_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created_date", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @Column(length = 100)
    private String customerName;

    @Column(length = 255)
    private String customerEmail;

    @Column(length = 20)
    private String customerPhone;

    @Column(length = 500)
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Add item to order.
     */
    public void addItem(OrderItem item) {
        if (!items.contains(item)) {
            items.add(item);
            item.setOrder(this);
        }
        recalculateTotals();
    }

    /**
     * Remove item from order.
     */
    public void removeItem(OrderItem item) {
        if (items.remove(item)) {
            item.setOrder(null);
        }
        recalculateTotals();
    }

    /**
     * Recalculate order totals based on items.
     */
    public void recalculateTotals() {
        this.subtotal = items.stream()
            .map(OrderItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.total = subtotal.add(tax).add(shippingCost);
    }

    /**
     * Mark order as delivered.
     */
    public void markAsDelivered() {
        if (!OrderStatus.SHIPPED.equals(status)) {
            throw new IllegalStateException("Order must be shipped before marking as delivered");
        }
        this.status = OrderStatus.DELIVERED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Get item count in order.
     */
    public Long getItemCount() {
        return items.stream()
            .mapToLong(OrderItem::getQuantity)
            .sum();
    }

    /**
     * Check if order can be cancelled.
     */
    public Boolean canBeCancelled() {
        return OrderStatus.PENDING.equals(status) || OrderStatus.CONFIRMED.equals(status);
    }

    /**
     * Cancel the order.
     */
    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * OrderStatus enum for order lifecycle (matches PDF spec).
     */
    public enum OrderStatus {
        PENDING,      // Initial state, awaiting confirmation
        CONFIRMED,    // Order confirmed and payment verified
        SHIPPED,      // Shipped to customer
        DELIVERED,    // Delivered to customer
        CANCELLED     // Cancelled order
    }
}
