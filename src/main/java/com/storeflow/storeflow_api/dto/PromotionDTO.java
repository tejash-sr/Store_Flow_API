package com.storeflow.storeflow_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Promotion.
 * Used for API requests and responses related to promotional discounts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDTO {

    private Long id;

    private String code;

    private BigDecimal discountPercentage;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean active;

    @JsonProperty("isActive")
    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
