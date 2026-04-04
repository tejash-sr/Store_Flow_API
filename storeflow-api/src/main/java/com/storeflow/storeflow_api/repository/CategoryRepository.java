package com.storeflow.storeflow_api.repository;

import com.storeflow.storeflow_api.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity providing data access operations.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find active category by name.
     */
    Optional<Category> findByNameAndIsActiveTrue(String name);

    /**
     * Find all active categories.
     */
    List<Category> findByIsActiveTrueOrderByNameAsc();

    /**
     * Count active categories.
     */
    Long countByIsActiveTrue();

    /**
     * Check if category exists by name (active only).
     */
    Boolean existsByNameAndIsActiveTrue(String name);

    /**
     * Find categories with products using custom query.
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products WHERE c.isActive = true")
    List<Category> findAllActiveWithProducts();
}
