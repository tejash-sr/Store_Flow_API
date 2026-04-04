package com.storeflow.storeflow_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * InventoryItem entity representing product stock at a specific store.
 * Tracks quantity on hand, minimum stock levels, and reorder information.
 */
@Entity
@Table(name = "inventory_items", indexes = {
    @Index(name = "idx_inventory_store_product", columnList = "store_id, product_id", unique = true),
    @Index(name = "idx_inventory_product_id", columnList = "product_id"),
    @Index(name = "idx_inventory_store_id", columnList = "store_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private Long quantityOnHand;

    @Column(nullable = false)
    @Builder.Default
    private Long minimumStockLevel = 10L;

    @Column(nullable = false)
    @Builder.Default
    private Long reorderQuantity = 50L;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_reordered_at")
    private LocalDateTime lastReorderedAt;

    /**
     * Check if inventory is below minimum level.
     */
    public Boolean isBelowMinimum() {
        return quantityOnHand < minimumStockLevel;
    }

    /**
     * Increase inventory quantity.
     */
    public void increaseQuantity(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantityOnHand += amount;
    }

    /**
     * Decrease inventory quantity.
     */
    public void decreaseQuantity(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantityOnHand < amount) {
            throw new IllegalArgumentException("Insufficient inventory. Available: " + quantityOnHand + ", Requested: " + amount);
        }
        this.quantityOnHand -= amount;
    }

    /**
     * Record reorder activity.
     */
    public void recordReorder() {
        this.lastReorderedAt = LocalDateTime.now();
    }

    /**
     * Check if item has available stock.
     */
    public Boolean hasStock(Long amount) {
        return quantityOnHand >= amount;
    }
}
