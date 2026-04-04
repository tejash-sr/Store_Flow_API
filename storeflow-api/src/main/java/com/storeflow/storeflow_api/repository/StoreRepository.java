package com.storeflow.storeflow_api.repository;

import com.storeflow.storeflow_api.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Store entity providing data access operations.
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * Find store by code.
     */
    Optional<Store> findByStoreCode(String storeCode);

    /**
     * Find all active stores.
     */
    List<Store> findByIsActiveTrueOrderByNameAsc();

    /**
     * Find stores by city.
     */
    List<Store> findByCityAndIsActiveTrueOrderByNameAsc(String city);

    /**
     * Count active stores.
     */
    Long countByIsActiveTrue();

    /**
     * Check if store code exists.
     */
    Boolean existsByStoreCode(String storeCode);

    /**
     * Find store with inventory for a product.
     */
    @Query("SELECT DISTINCT s FROM Store s " +
           "LEFT JOIN FETCH s.inventoryItems i " +
           "WHERE s.isActive = true " +
           "ORDER BY s.name")
    List<Store> findAllActiveWithInventory();

    /**
     * Find stores by state.
     */
    List<Store> findByStateAndIsActiveTrueOrderByNameAsc(String state);
}
