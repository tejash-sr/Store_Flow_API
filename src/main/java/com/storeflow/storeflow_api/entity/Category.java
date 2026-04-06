package com.storeflow.storeflow_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({"products", "subcategories", "parent", "rootCategory"})
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
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_category_parent"))
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<Category> subcategories = new ArrayList<>();

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

    /**
     * Set parent category for hierarchical structure.
     */
    public void setParentCategory(Category parentCategory) {
        this.parent = parentCategory;
        if (parentCategory != null && !parentCategory.subcategories.contains(this)) {
            parentCategory.subcategories.add(this);
        }
    }

    /**
     * Add subcategory to this category.
     */
    public void addSubcategory(Category subcategory) {
        if (!subcategories.contains(subcategory)) {
            subcategories.add(subcategory);
            subcategory.parent = this;
        }
    }

    /**
     * Remove subcategory from this category.
     */
    public void removeSubcategory(Category subcategory) {
        if (subcategories.contains(subcategory)) {
            subcategories.remove(subcategory);
            subcategory.parent = null;
        }
    }

    /**
     * Check if this category is a parent of another category.
     */
    public boolean isParentOf(Category other) {
        return other.parent != null && other.parent.equals(this);
    }

    /**
     * Get the root category in the hierarchy.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Category getRootCategory() {
        if (parent == null) {
            return this;
        }
        return parent.getRootCategory();
    }
}
