package com.storeflow.storeflow_api.controller;

import com.storeflow.storeflow_api.dto.PromotionDTO;
import com.storeflow.storeflow_api.entity.Promotion;
import com.storeflow.storeflow_api.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Promotion management.
 * Provides endpoints for creating, updating, retrieving, and managing
 * promotional discounts and their application to products.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotions", description = "APIs for managing product promotions and discounts")
public class PromotionController {

    private final PromotionService promotionService;

    /**
     * Create a new promotion.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new promotion", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<PromotionDTO> createPromotion(@RequestBody PromotionDTO promotionDTO) {
        log.info("Creating new promotion with code: {}", promotionDTO.getCode());
        Promotion promotion = new Promotion();
        promotion.setCode(promotionDTO.getCode());
        promotion.setDiscountPercentage(promotionDTO.getDiscountPercentage());
        promotion.setStartDate(promotionDTO.getStartDate());
        promotion.setEndDate(promotionDTO.getEndDate());
        promotion.setActive(promotionDTO.getActive());

        Promotion createdPromotion = promotionService.createPromotion(promotion);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(createdPromotion));
    }

    /**
     * Get promotion by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get promotion by ID")
    public ResponseEntity<PromotionDTO> getPromotion(@PathVariable Long id) {
        log.info("Fetching promotion with ID: {}", id);
        return promotionService.getPromotionById(id)
            .map(promotion -> ResponseEntity.ok(convertToDTO(promotion)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get promotion by code.
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "Get promotion by code")
    public ResponseEntity<PromotionDTO> getPromotionByCode(@PathVariable String code) {
        log.info("Fetching promotion with code: {}", code);
        return promotionService.getPromotionByCode(code)
            .map(promotion -> ResponseEntity.ok(convertToDTO(promotion)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all promotions for a product.
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "Get all promotions for a product")
    public ResponseEntity<List<PromotionDTO>> getProductPromotions(
        @PathVariable @Parameter(description = "Product ID") Long productId) {
        log.info("Fetching promotions for product ID: {}", productId);
        List<Promotion> promotions = promotionService.getPromotionsForProduct(productId);
        List<PromotionDTO> promotionDTOs = promotions.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(promotionDTOs);
    }

    /**
     * Get active promotion for a product.
     */
    @GetMapping("/product/{productId}/active")
    @Operation(summary = "Get active promotion for a product")
    public ResponseEntity<PromotionDTO> getActivePromotionForProduct(
        @PathVariable @Parameter(description = "Product ID") Long productId) {
        log.info("Fetching active promotion for product ID: {}", productId);
        return promotionService.getActivePromotionForProduct(productId)
            .map(promotion -> ResponseEntity.ok(convertToDTO(promotion)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all currently active promotions.
     */
    @GetMapping("/active")
    @Operation(summary = "Get all currently active promotions")
    public ResponseEntity<List<PromotionDTO>> getCurrentlyActivePromotions() {
        log.info("Fetching all currently active promotions");
        List<Promotion> promotions = promotionService.getCurrentlyActivePromotions();
        List<PromotionDTO> promotionDTOs = promotions.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(promotionDTOs);
    }

    /**
     * Get upcoming promotions.
     */
    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming promotions")
    public ResponseEntity<List<PromotionDTO>> getUpcomingPromotions() {
        log.info("Fetching upcoming promotions");
        List<Promotion> promotions = promotionService.getUpcomingPromotions();
        List<PromotionDTO> promotionDTOs = promotions.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(promotionDTOs);
    }

    /**
     * Update promotion.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update promotion", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<PromotionDTO> updatePromotion(
        @PathVariable Long id,
        @RequestBody PromotionDTO promotionDTO) {
        log.info("Updating promotion with ID: {}", id);
        Promotion promotionDetails = new Promotion();
        promotionDetails.setCode(promotionDTO.getCode());
        promotionDetails.setDiscountPercentage(promotionDTO.getDiscountPercentage());
        promotionDetails.setStartDate(promotionDTO.getStartDate());
        promotionDetails.setEndDate(promotionDTO.getEndDate());
        promotionDetails.setActive(promotionDTO.getActive());

        Promotion updatedPromotion = promotionService.updatePromotion(id, promotionDetails);
        return ResponseEntity.ok(convertToDTO(updatedPromotion));
    }

    /**
     * Deactivate promotion.
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate promotion", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<PromotionDTO> deactivatePromotion(@PathVariable Long id) {
        log.info("Deactivating promotion with ID: {}", id);
        Promotion promotion = promotionService.deactivatePromotion(id);
        return ResponseEntity.ok(convertToDTO(promotion));
    }

    /**
     * Delete promotion.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete promotion", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        log.info("Deleting promotion with ID: {}", id);
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Convert Promotion to PromotionDTO.
     */
    private PromotionDTO convertToDTO(Promotion promotion) {
        return PromotionDTO.builder()
            .id(promotion.getId())
            .code(promotion.getCode())
            .discountPercentage(promotion.getDiscountPercentage())
            .startDate(promotion.getStartDate())
            .endDate(promotion.getEndDate())
            .active(promotion.getActive())
            .isActive(promotion.isActive())
            .createdAt(promotion.getCreatedAt())
            .updatedAt(promotion.getUpdatedAt())
            .build();
    }
}
