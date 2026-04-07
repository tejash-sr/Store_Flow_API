package com.storeflow.storeflow_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Store entity representing physical or virtual store locations.
 * Manages store-specific inventory and orders.
 */
@Entity
@Table(name = "stores", indexes = {
    @Index(name = "idx_store_code", columnList = "store_code", unique = true),
    @Index(name = "idx_store_name", columnList = "name"),
    @Index(name = "idx_store_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String storeCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 20)
    private String city;

    @Column(length = 20)
    private String state;

    @Column(length = 10)
    private String zipCode;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 255)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<InventoryItem> inventoryItems = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Soft delete for stores.
     */
    public void softDelete() {
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restore a soft-deleted store.
     */
    public void restore() {
        this.isActive = true;
        this.deletedAt = null;
    }

    /**
     * Add inventory item to store.
     */
    public void addInventoryItem(InventoryItem item) {
        if (!inventoryItems.contains(item)) {
            inventoryItems.add(item);
            item.setStore(this);
        }
    }
}
