package com.storeflow.storeflow_api.specification;

import com.storeflow.storeflow_api.entity.Product;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

/**
 * JPA Specification for dynamic Product queries.
 * Supports filtering by multiple criteria: name, category, price range, and status.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductSpecification {

    /**
     * Create a Specification with multiple filter criteria
     *
     * @param name           Product name (substring search, case-insensitive)
     * @param categoryId     Category ID (exact match)
     * @param minPrice       Minimum price (inclusive)
     * @param maxPrice       Maximum price (inclusive)
     * @param isActive       Product active status (exact match)
     * @return Specification for dynamic Product queries
     */
    public static Specification<Product> withFilters(String name, Long categoryId, BigDecimal minPrice,
                                                      BigDecimal maxPrice, Boolean isActive) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Name filter: case-insensitive substring search
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(
                    cb.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
                ));
            }

            // Category filter: exact match
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            // Price range filters
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // Active status filter: exact match
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter by product name only (case-insensitive substring)
     *
     * @param name Product name to search
     * @return Specification for name matching
     */
    public static Specification<Product> nameContains(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    /**
     * Filter by category ID
     *
     * @param categoryId Category ID to match
     * @return Specification for category matching
     */
    public static Specification<Product> categoryIdEquals(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    /**
     * Filter by price range
     *
     * @param minPrice Minimum price (inclusive)
     * @param maxPrice Maximum price (inclusive)
     * @return Specification for price range matching
     */
    public static Specification<Product> priceInRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter by product active status
     *
     * @param isActive Product active status to match
     * @return Specification for active status matching
     */
    public static Specification<Product> isActiveEquals(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("isActive"), isActive);
        };
    }

    /**
     * Filter for active products only
     *
     * @return Specification matching active products
     */
    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.equal(root.get("isActive"), true);
    }
}
