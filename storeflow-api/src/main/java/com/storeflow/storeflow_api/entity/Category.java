package com.storeflow.storeflow_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Category entity representing a product category in the inventory system.
 * Maintains relationships with products and supports soft deletes.
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_name", columnList = "name"),
    @Index(name = "idx_category_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Product> products = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Soft delete for categories - marks as deleted without removing data.
     */
    public void softDelete() {
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restore a soft-deleted category.
     */
    public void restore() {
        this.isActive = true;
        this.deletedAt = null;
    }

    /**
     * Add product to category.
     */
    public void addProduct(Product product) {
        if (!products.contains(product)) {
            products.add(product);
            product.setCategory(this);
        }
    }

    /**
     * Remove product from category.
     */
    public void removeProduct(Product product) {
        if (products.contains(product)) {
            products.remove(product);
            product.setCategory(null);
        }
    }
}
