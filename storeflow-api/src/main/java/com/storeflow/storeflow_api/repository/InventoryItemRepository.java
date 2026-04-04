package com.storeflow.storeflow_api.repository;

import com.storeflow.storeflow_api.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for InventoryItem entity providing data access operations.
 */
@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    /**
     * Find inventory for specific product and store.
     */
    Optional<InventoryItem> findByProduct_IdAndStore_Id(Long productId, Long storeId);

    /**
     * Find all inventory for a product across stores.
     */
    List<InventoryItem> findByProduct_IdOrderByStore_NameAsc(Long productId);

    /**
     * Find all inventory for a store.
     */
    List<InventoryItem> findByStore_IdOrderByProduct_NameAsc(Long storeId);

    /**
     * Find inventory items below minimum stock level.
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.quantityOnHand < i.minimumStockLevel ORDER BY i.product.name")
    List<InventoryItem> findBelowMinimumStockLevel();

    /**
     * Find inventory items below minimum for specific store.
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.store.id = :storeId AND i.quantityOnHand < i.minimumStockLevel ORDER BY i.product.name")
    List<InventoryItem> findBelowMinimumStockLevelForStore(@Param("storeId") Long storeId);

    /**
     * Find total quantity for a product across all stores.
     */
    @Query("SELECT COALESCE(SUM(i.quantityOnHand), 0) FROM InventoryItem i WHERE i.product.id = :productId")
    Long getTotalQuantityForProduct(@Param("productId") Long productId);

    /**
     * Find inventory items with zero quantity.
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.quantityOnHand = 0 ORDER BY i.product.name")
    List<InventoryItem> findOutOfStock();

    /**
     * Count stores carrying a product.
     */
    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.product.id = :productId")
    Long countStoresCarryingProduct(@Param("productId") Long productId);
}
