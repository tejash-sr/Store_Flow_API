package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.entity.Promotion;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.repository.PromotionRepository;
import com.storeflow.storeflow_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing promotions.
 * Handles business logic for creating, updating, querying, and managing
 * promotional discounts and their application to products.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;

    /**
     * Create a new promotion.
     */
    public Promotion createPromotion(Promotion promotion) {
        log.debug("Creating new promotion with code: {}", promotion.getCode());
        return promotionRepository.save(promotion);
    }

    /**
     * Update an existing promotion.
     */
    public Promotion updatePromotion(Long promotionId, Promotion promotionDetails) {
        log.debug("Updating promotion with ID: {}", promotionId);
        Promotion promotion = promotionRepository.findById(promotionId)
            .orElseThrow(() -> new IllegalArgumentException("Promotion not found with ID: " + promotionId));

        promotion.setCode(promotionDetails.getCode());
        promotion.setDiscountPercentage(promotionDetails.getDiscountPercentage());
        promotion.setStartDate(promotionDetails.getStartDate());
        promotion.setEndDate(promotionDetails.getEndDate());
        promotion.setActive(promotionDetails.getActive());

        return promotionRepository.save(promotion);
    }

    /**
     * Get promotion by ID.
     */
    @Transactional(readOnly = true)
    public Optional<Promotion> getPromotionById(Long promotionId) {
        log.debug("Fetching promotion with ID: {}", promotionId);
        return promotionRepository.findById(promotionId);
    }

    /**
     * Get promotion by code.
     */
    @Transactional(readOnly = true)
    public Optional<Promotion> getPromotionByCode(String code) {
        log.debug("Fetching promotion with code: {}", code);
        return promotionRepository.findByCode(code);
    }

    /**
     * Get all promotions for a product.
     */
    @Transactional(readOnly = true)
    public List<Promotion> getPromotionsForProduct(Long productId) {
        log.debug("Fetching all promotions for product ID: {}", productId);
        return promotionRepository.findByProductId(productId);
    }

    /**
     * Get active promotion for a product.
     */
    @Transactional(readOnly = true)
    public Optional<Promotion> getActivePromotionForProduct(Long productId) {
        log.debug("Fetching active promotion for product ID: {}", productId);
        return promotionRepository.findActivePromotionForProduct(productId);
    }

    /**
     * Get all currently active promotions (globally).
     */
    @Transactional(readOnly = true)
    public List<Promotion> getCurrentlyActivePromotions() {
        log.debug("Fetching all currently active promotions");
        return promotionRepository.findCurrentlyActivePromotions();
    }

    /**
     * Get upcoming promotions.
     */
    @Transactional(readOnly = true)
    public List<Promotion> getUpcomingPromotions() {
        log.debug("Fetching upcoming promotions");
        return promotionRepository.findUpcomingPromotions();
    }

    /**
     * Get expired promotions.
     */
    @Transactional(readOnly = true)
    public List<Promotion> getExpiredPromotions() {
        log.debug("Fetching expired promotions");
        return promotionRepository.findExpiredPromotions();
    }

    /**
     * Get promotions within a date range.
     */
    @Transactional(readOnly = true)
    public List<Promotion> getPromotionsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching promotions in date range: {} to {}", startDate, endDate);
        return promotionRepository.findPromotionsInDateRange(startDate, endDate);
    }

    /**
     * Deactivate a promotion.
     */
    public Promotion deactivatePromotion(Long promotionId) {
        log.debug("Deactivating promotion with ID: {}", promotionId);
        Promotion promotion = promotionRepository.findById(promotionId)
            .orElseThrow(() -> new IllegalArgumentException("Promotion not found with ID: " + promotionId));
        promotion.deactivate();
        return promotionRepository.save(promotion);
    }

    /**
     * Activate a promotion.
     */
    public Promotion activatePromotion(Long promotionId) {
        log.debug("Activating promotion with ID: {}", promotionId);
        Promotion promotion = promotionRepository.findById(promotionId)
            .orElseThrow(() -> new IllegalArgumentException("Promotion not found with ID: " + promotionId));
        promotion.activate();
        return promotionRepository.save(promotion);
    }

    /**
     * Delete a promotion.
     */
    public void deletePromotion(Long promotionId) {
        log.debug("Deleting promotion with ID: {}", promotionId);
        promotionRepository.deleteById(promotionId);
    }

    /**
     * Check if a promotion code already exists.
     */
    @Transactional(readOnly = true)
    public boolean isPromotionCodeExists(String code) {
        return promotionRepository.findByCode(code).isPresent();
    }
}
