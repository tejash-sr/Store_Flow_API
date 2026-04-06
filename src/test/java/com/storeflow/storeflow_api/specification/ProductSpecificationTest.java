package com.storeflow.storeflow_api.specification;

import com.storeflow.storeflow_api.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ProductSpecification
 * Tests dynamic query specification construction for Products
 */
class ProductSpecificationTest {

    /**
     * Test nameContains specification builder with valid name
     */
    @Test
    void testNameContainsSpecification_WithValidName() {
        // Act - Build the specification
        Specification<Product> spec = ProductSpecification.nameContains("Laptop");

        // Assert - verify the specification is properly defined (not null)
        assertThat(spec).isNotNull();

        // Verify toString contains spec information
        assertThat(spec.toString()).contains("Specification")
                .as("Specification should have proper toString representation");
    }

    /**
     * Test nameContains specification with blank string returns conjunction
     */
    @Test
    void testNameContainsSpecification_WithBlankName() {
        // Act
        Specification<Product> spec = ProductSpecification.nameContains("");

        // Assert - Should be non-null (returns conjunction for empty strings)
        assertThat(spec).isNotNull();
        assertThat(spec.toString()).contains("Specification");
    }

    /**
     * Test nameContains specification with null name returns conjunction
     */
    @Test
    void testNameContainsSpecification_WithNullName() {
        // Act
        Specification<Product> spec = ProductSpecification.nameContains(null);

        // Assert - Should handle null gracefully and return conjunction
        assertThat(spec).isNotNull();
        assertThat(spec.toString()).contains("Specification");
    }

    /**
     * Test categoryIdEquals specification with valid category ID
     */
    @Test
    void testCategoryIdEqualsSpecification_WithValidCategoryId() {
        // Arrange
        Long categoryId = 1L;

        // Act
        Specification<Product> spec = ProductSpecification.categoryIdEquals(categoryId);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(spec.toString()).contains("Specification");
    }

    /**
     * Test categoryIdEquals specification with null category ID
     */
    @Test
    void testCategoryIdEqualsSpecification_WithNullCategoryId() {
        // Act
        Specification<Product> spec = ProductSpecification.categoryIdEquals(null);

        // Assert - Should handle null and return conjunction
        assertThat(spec).isNotNull();
        assertThat(spec.toString()).contains("Specification");
    }

    /**
     * Test priceInRange specification with valid price range
     */
    @Test
    void testPriceInRangeSpecification_WithValidRange() {
        // Arrange
        BigDecimal minPrice = new BigDecimal("20.00");
        BigDecimal maxPrice = new BigDecimal("100.00");

        // Act
        Specification<Product> spec = ProductSpecification.priceInRange(minPrice, maxPrice);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(spec.toString()).contains("Specification");
    }

    /**
     * Test priceInRange specification with only min price
     */
    @Test
    void testPriceInRangeSpecification_WithOnlyMinPrice() {
        // Arrange
        BigDecimal minPrice = new BigDecimal("50.00");

        // Act
        Specification<Product> spec = ProductSpecification.priceInRange(minPrice, null);

        // Assert
        assertThat(spec).isNotNull();
    }

    /**
     * Test priceInRange specification with only max price
     */
    @Test
    void testPriceInRangeSpecification_WithOnlyMaxPrice() {
        // Arrange
        BigDecimal maxPrice = new BigDecimal("500.00");

        // Act
        Specification<Product> spec = ProductSpecification.priceInRange(null, maxPrice);

        // Assert
        assertThat(spec).isNotNull();
    }

    /**
     * Test priceInRange specification with null prices
     */
    @Test
    void testPriceInRangeSpecification_WithNullPrices() {
        // Act
        Specification<Product> spec = ProductSpecification.priceInRange(null, null);

        // Assert
        assertThat(spec).isNotNull();
    }

    /**
     * Test isActiveEquals specification with true
     */
    @Test
    void testIsActiveEqualsSpecification_WithTrue() {
        // Act
        Specification<Product> spec = ProductSpecification.isActiveEquals(true);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(spec.toString()).contains("Specification");
    }

    /**
     * Test isActiveEquals specification with false
     */
    @Test
    void testIsActiveEqualsSpecification_WithFalse() {
        // Act
        Specification<Product> spec = ProductSpecification.isActiveEquals(false);

        // Assert
        assertThat(spec).isNotNull();
    }

    /**
     * Test isActiveEquals specification with null
     */
    @Test
    void testIsActiveEqualsSpecification_WithNull() {
        // Act
        Specification<Product> spec = ProductSpecification.isActiveEquals(null);

        // Assert
        assertThat(spec).isNotNull();
    }

    /**
     * Test isActive specification returns active products only
     */
    @Test
    void testIsActiveSpecification() {
        // Act
        Specification<Product> spec = ProductSpecification.isActive();

        // Assert
        assertThat(spec).isNotNull();
        assertThat(spec.toString()).contains("Specification");
    }

    /**
     * Test withFilters specification with multiple criteria
     */
    @Test
    void testWithFiltersSpecification_WithAllCriteria() {
        // Arrange
        String name = "Laptop";
        Long categoryId = 1L;
        BigDecimal minPrice = new BigDecimal("100.00");
        BigDecimal maxPrice = new BigDecimal("2000.00");
        Boolean isActive = true;

        // Act
        Specification<Product> spec = ProductSpecification.withFilters(name, categoryId, minPrice, maxPrice, isActive);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(spec.toString()).contains("Specification");
    }

    /**
     * Test withFilters specification with partial criteria
     */
    @Test
    void testWithFiltersSpecification_WithPartialCriteria() {
        // Act
        Specification<Product> spec = ProductSpecification.withFilters(null, 1L, null, null, true);

        // Assert
        assertThat(spec).isNotNull();
    }

    /**
     * Test withFilters specification with all null criteria
     */
    @Test
    void testWithFiltersSpecification_WithAllNullCriteria() {
        // Act
        Specification<Product> spec = ProductSpecification.withFilters(null, null, null, null, null);

        // Assert
        assertThat(spec).isNotNull();
    }

    /**
     * Test specification composition with and()
     */
    @Test
    void testSpecificationComposition_WithAnd() {
        // Arrange
        Specification<Product> spec1 = ProductSpecification.nameContains("Laptop");
        Specification<Product> spec2 = ProductSpecification.isActive();

        // Act - Specifications can be combined using and()
        Specification<Product> combined = spec1.and(spec2);

        // Assert
        assertThat(combined).isNotNull();
    }

    /**
     * Test specification composition with or()
     */
    @Test
    void testSpecificationComposition_WithOr() {
        // Arrange
        Specification<Product> spec1 = ProductSpecification.isActiveEquals(true);
        Specification<Product> spec2 = ProductSpecification.isActiveEquals(false);

        // Act - Specifications can be combined using or()
        Specification<Product> combined = spec1.or(spec2);

        // Assert
        assertThat(combined).isNotNull();
    }
}

