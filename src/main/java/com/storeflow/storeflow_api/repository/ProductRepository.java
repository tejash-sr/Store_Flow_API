package com.storeflow.storeflow_api.repository;

import com.storeflow.storeflow_api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entity providing data access operations.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * Find product by SKU.
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find active products by category.
     */
    List<Product> findByCategory_IdAndIsActiveTrueOrderByNameAsc(Long categoryId);

    /**
     * Find products by price range.
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.name")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find all active products.
     */
    List<Product> findByIsActiveTrueOrderByNameAsc();

    /**
     * Find all active products with pagination and sorting.
     */
    Page<Product> findByIsActiveTrue(Pageable pageable);

    /**
     * Count active products.
     */
    Long countByIsActiveTrue();

    /**
     * Check if SKU exists.
     */
    Boolean existsBySku(String sku);

    /**
     * Find products with low inventory across stores.
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "JOIN p.inventoryItems i " +
           "WHERE p.isActive = true AND i.quantityOnHand < i.minimumStockLevel " +
           "ORDER BY p.name")
    List<Product> findProductsWithLowInventory();

    /**
     * Find products from specific category with inventory.
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.inventoryItems " +
           "WHERE p.category.id = :categoryId AND p.isActive = true " +
           "ORDER BY p.name")
    List<Product> findByCategoryWithInventory(@Param("categoryId") Long categoryId);
}
