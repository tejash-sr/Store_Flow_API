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
 * Product entity representing items in the inventory system.
 * Supports multiple categories, pricing, and inventory tracking.
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_sku", columnList = "sku", unique = true),
    @Index(name = "idx_product_name", columnList = "name"),
    @Index(name = "idx_product_category_id", columnList = "category_id"),
    @Index(name = "idx_product_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<InventoryItem> inventoryItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Soft delete for products.
     */
    public void softDelete() {
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restore a soft-deleted product.
     */
    public void restore() {
        this.isActive = true;
        this.deletedAt = null;
    }

    /**
     * Calculate profit margin.
     */
    public BigDecimal getProfitMargin() {
        if (cost == null || cost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return price.subtract(cost);
    }

    /**
     * Calculate profit margin percentage.
     */
    public BigDecimal getProfitMarginPercentage() {
        BigDecimal margin = getProfitMargin();
        if (margin.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return margin.divide(price, 2, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100));
    }

    /**
     * Get total inventory across all stores.
     */
    public Long getTotalInventory() {
        return inventoryItems.stream()
            .mapToLong(InventoryItem::getQuantityOnHand)
            .sum();
    }

    /**
     * Add inventory item for a store.
     */
    public void addInventoryItem(InventoryItem item) {
        if (!inventoryItems.contains(item)) {
            inventoryItems.add(item);
            item.setProduct(this);
        }
    }
}
