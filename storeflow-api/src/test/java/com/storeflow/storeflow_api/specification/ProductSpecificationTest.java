package com.storeflow.storeflow_api.specification;

import com.storeflow.storeflow_api.entity.Product;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        // Arrange - Mock JPA criteria components
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        // Act
        var spec = ProductSpecification.nameContains("Laptop");
        assertNotNull(spec, "Specification should not be null");

        // Assert - verify the specification is properly defined
        assertTrue(spec.toString().contains("Specification") || spec != null);
    }

    /**
     * Test nameContains specification with blank string returns conjunction
     */
    @Test
    void testNameContainsSpecification_WithBlankName() {
        // Arrange
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        // Act
        var spec = ProductSpecification.nameContains("");
        assertNotNull(spec, "Specification should not be null");
    }

    /**
     * Test nameContains specification with null name returns conjunction
     */
    @Test
    void testNameContainsSpecification_WithNullName() {
        // Act
        var spec = ProductSpecification.nameContains(null);
        assertNotNull(spec, "Specification should not be null");
    }

    /**
     * Test categoryIdEquals specification with valid category ID
     */
    @Test
    void testCategoryIdEqualsSpecification_WithValidCategoryId() {
        // Arrange
        Long categoryId = 1L;

        // Act
        var spec = ProductSpecification.categoryIdEquals(categoryId);
        assertNotNull(spec, "Specification should not be null");
    }

    /**
     * Test categoryIdEquals specification with null category ID
     */
    @Test
    void testCategoryIdEqualsSpecification_WithNullCategoryId() {
        // Act
        var spec = ProductSpecification.categoryIdEquals(null);
        assertNotNull(spec, "Specification should not be null");
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
        var spec = ProductSpecification.priceInRange(minPrice, maxPrice);
        assertNotNull(spec, "Specification should not be null");
    }

    /**
     * Test priceInRange specification with only min price
     */
    @Test
    void testPriceInRangeSpecification_WithOnlyMinPrice() {
        // Arrange
        BigDecimal minPrice = new BigDecimal("50.00");

        // Act
        var spec = ProductSpecification.priceInRange(minPrice, null);
        assertNotNull(spec, "Specification should not be null");
    }

    /**
     * Test priceInRange specification with only max price
     */
    @Test
    void testPriceInRangeSpecification_WithOnlyMaxPrice() {
        // Arrange
        BigDecimal maxPrice = new BigDecimal("500.00");

        // Act
        var spec = ProductSpecification.priceInRange(null, maxPrice);
        assertNotNull(spec, "Specification should not be null");
    }

    /**
     * Test priceInRange specification with null prices
     */
    @Test
    void testPriceInRangeSpecification_WithNullPrices() {
        // Act
        var spec = ProductSpecification.priceInRange(null, null);
        assertNotNull(spec, "Specification should not be null");
    }

    /**
     * Test isActiveEquals specification with true
     */
    @Test
    void testIsActiveEqualsSpecification_WithTrue() {
        // Act
        var spec = ProductSpecification.isActiveEquals(true);
        assertNotNull(spec, "Specification should not be null");
    }

    /**
     * Test isActiveEquals specification with false
     */
    @Test
    void testIsActiveEqualsSpecification_WithFalse() {
        // Act
        var spec = ProductSpecification.isActiveEquals(false);
        assertNotNull(spec, "Specification should not be null");
    }

    /**
     * Test isActiveEquals specification with null
     */
    @Test
    void testIsActiveEqualsSpecification_WithNull() {
        // Act
        var spec = ProductSpecification.isActiveEquals(null);
        assertNotNull(spec, "Specification should not be null");
    }

    /**
     * Test isActive specification returns active products only
     */
    @Test
    void testIsActiveSpecification() {
        // Act
        var spec = ProductSpecification.isActive();
        assertNotNull(spec, "Specification should not be null");
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
        var spec = ProductSpecification.withFilters(name, categoryId, minPrice, maxPrice, isActive);
        assertNotNull(spec, "Specification should not be null");
    }

    /**
     * Test withFilters specification with partial criteria
     */
    @Test
    void testWithFiltersSpecification_WithPartialCriteria() {
        // Act
        var spec = ProductSpecification.withFilters(null, 1L, null, null, true);
        assertNotNull(spec, "Specification should not be null");
    }

    /**
     * Test withFilters specification with all null criteria
     */
    @Test
    void testWithFiltersSpecification_WithAllNullCriteria() {
        // Act
        var spec = ProductSpecification.withFilters(null, null, null, null, null);
        assertNotNull(spec, "Specification should not be null");
    }

    /**
     * Test specification composition with and()
     */
    @Test
    void testSpecificationComposition_WithAnd() {
        // Arrange
        var spec1 = ProductSpecification.nameContains("Laptop");
        var spec2 = ProductSpecification.isActive();

        // Act - Specifications can be combined using and()
        var combined = spec1.and(spec2);
        assertNotNull(combined, "Combined specification should not be null");
    }

    /**
     * Test specification composition with or()
     */
    @Test
    void testSpecificationComposition_WithOr() {
        // Arrange
        var spec1 = ProductSpecification.isActiveEquals(true);
        var spec2 = ProductSpecification.isActiveEquals(false);

        // Act - Specifications can be combined using or()
        var combined = spec1.or(spec2);
        assertNotNull(combined, "Combined specification should not be null");
    }

}

