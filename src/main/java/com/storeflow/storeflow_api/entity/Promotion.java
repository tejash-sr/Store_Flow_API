package com.storeflow.storeflow_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Promotion entity representing time-limited promotional discounts for products.
 * Stores discount codes, percentages, and validity periods for seasonal promotions
 * and special offers per system PDF specification.
 */
@Entity
@Table(name = "promotions", indexes = {
    @Index(name = "idx_promotion_product_id", columnList = "product_id"),
    @Index(name = "idx_promotion_code", columnList = "code"),
    @Index(name = "idx_promotion_active", columnList = "active"),
    @Index(name = "idx_promotion_dates", columnList = "start_date, end_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_promotion_product"))
    private Product product;

    @Column(nullable = false, length = 100, unique = true)
    private String code;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if promotion is currently active based on dates and active flag.
     */
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return this.active && now.isAfter(startDate) && now.isBefore(endDate);
    }

    /**
     * Check if promotion has started.
     */
    public boolean hasStarted() {
        return LocalDateTime.now().isAfter(startDate);
    }

    /**
     * Check if promotion has ended.
     */
    public boolean hasEnded() {
        return LocalDateTime.now().isAfter(endDate);
    }

    /**
     * Deactivate the promotion.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Activate the promotion.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Calculate discount amount for a given price.
     */
    public BigDecimal calculateDiscount(BigDecimal originalPrice) {
        return originalPrice.multiply(discountPercentage).divide(new BigDecimal(100));
    }

    /**
     * Calculate discounted price.
     */
    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice) {
        return originalPrice.subtract(calculateDiscount(originalPrice));
    }
}
