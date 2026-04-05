package com.storeflow.storeflow_api.repository;

import com.storeflow.storeflow_api.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Promotion entity.
 * Provides database access methods for managing promotions, including
 * querying by product, code, and promotional status.
 */
@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /**
     * Find promotion by code.
     */
    Optional<Promotion> findByCode(String code);

    /**
     * Find all active promotions for a product.
     */
    List<Promotion> findByProductIdAndActive(Long productId, Boolean active);

    /**
     * Find all promotions for a product.
     */
    List<Promotion> findByProductId(Long productId);

    /**
     * Find all active promotions (globally).
     */
    List<Promotion> findByActive(Boolean active);

    /**
     * Find promotions that are currently active (within start/end date range).
     */
    @Query("SELECT p FROM Promotion p WHERE p.active = true " +
           "AND p.startDate <= CURRENT_TIMESTAMP " +
           "AND p.endDate > CURRENT_TIMESTAMP")
    List<Promotion> findCurrentlyActivePromotions();

    /**
     * Find promotions for a product that are currently active (within date range).
     */
    @Query("SELECT p FROM Promotion p WHERE p.product.id = :productId " +
           "AND p.active = true " +
           "AND p.startDate <= CURRENT_TIMESTAMP " +
           "AND p.endDate > CURRENT_TIMESTAMP")
    Optional<Promotion> findActivePromotionForProduct(@Param("productId") Long productId);

    /**
     * Find upcoming promotions (not yet started).
     */
    @Query("SELECT p FROM Promotion p WHERE p.startDate > CURRENT_TIMESTAMP " +
           "ORDER BY p.startDate ASC")
    List<Promotion> findUpcomingPromotions();

    /**
     * Find expired promotions.
     */
    @Query("SELECT p FROM Promotion p WHERE p.endDate <= CURRENT_TIMESTAMP")
    List<Promotion> findExpiredPromotions();

    /**
     * Find promotions within a date range.
     */
    @Query("SELECT p FROM Promotion p WHERE p.startDate <= :endDate " +
           "AND p.endDate >= :startDate")
    List<Promotion> findPromotionsInDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
}
