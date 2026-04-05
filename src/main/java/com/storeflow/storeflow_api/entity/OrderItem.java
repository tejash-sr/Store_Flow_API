package com.storeflow.storeflow_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderItem entity representing a single line item in an order.
 * Maintains snapshot of product price and calculates line totals.
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order_id", columnList = "order_id"),
    @Index(name = "idx_order_item_product_id", columnList = "product_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Long quantity;

    /**
     * Price snapshot at time of order (in case product price changes).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(length = 255)
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Calculate subtotal for this line item (before discount).
     */
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(new BigDecimal(quantity));
    }

    /**
     * Calculate line total (after discount).
     */
    public BigDecimal getLineTotal() {
        BigDecimal subtotal = getSubtotal();
        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            return subtotal.subtract(discountAmount);
        }
        return subtotal;
    }

    /**
     * Calculate discount percentage.
     */
    public BigDecimal getDiscountPercentage() {
        BigDecimal subtotal = getSubtotal();
        if (subtotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return discountAmount.divide(subtotal, 2, java.math.RoundingMode.HALF_UP)
            .multiply(new BigDecimal(100));
    }

    /**
     * Validate quantity is positive.
     */
    @PrePersist
    @PreUpdate
    public void validateQuantity() {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
    }
}
