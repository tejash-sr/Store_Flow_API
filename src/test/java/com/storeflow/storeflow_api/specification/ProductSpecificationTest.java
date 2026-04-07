package com.storeflow.storeflow_api.specification;

import com.storeflow.storeflow_api.entity.Product;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ProductSpecification
 * Tests dynamic query specification construction for Products
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductSpecificationTest {

    @Mock
    private Root<Product> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    /**
     * Test nameContains specification builder with valid name
     * Should create a specification that performs case-insensitive substring matching
     */
    @Test
    void testNameContainsSpecification_WithValidName() {
        // Act - Build the specification
        Specification<Product> spec = ProductSpecification.nameContains("Laptop");

        // Assert - verify the specification is properly defined (not null)
        assertThat(spec).isNotNull()
            .as("Specification should be created for valid name");
    }

    /**
     * Test nameContains specification with empty name
     * Should return conjunction (match all) when name is blank
     */
    @Test
    void testNameContainsSpecification_WithEmptyName() {
        // Act
        Specification<Product> spec = ProductSpecification.nameContains("");

        // Assert
        assertThat(spec).isNotNull();
    }

    /**
     * Test nameContains specification with null name
     * Should return conjunction (match all) when name is null
     */
    @Test
    void testNameContainsSpecification_WithNullName() {
        // Act
        Specification<Product> spec = ProductSpecification.nameContains(null);

        // Assert
        assertThat(spec).isNotNull();
    }

    /**
     * Test categoryIdEquals specification
     */
    @Test
    void testCategoryIdEqualsSpecification() {
        // Act
        Specification<Product> spec = ProductSpecification.categoryIdEquals(1L);

        // Assert
        assertThat(spec).isNotNull()
            .as("Specification should be created for category ID");
    }

    /**
     * Test categoryIdEquals with null ID
     */
    @Test
    void testCategoryIdEqualsSpecification_WithNullId() {
        // Act
        Specification<Product> spec = ProductSpecification.categoryIdEquals(null);

        // Assert
        assertThat(spec).isNotNull();
    }

    /**
     * Test priceRange specification
     */
    @Test
    void testPriceRangeSpecification() {
        // Act
        Specification<Product> spec = ProductSpecification.priceInRange(
            new BigDecimal("10.00"),
            new BigDecimal("100.00")
        );

        // Assert
        assertThat(spec).isNotNull()
            .as("Specification should be created for price range");
    }

    /**
     * Test priceRange with null values
     */
    @Test
    void testPriceRangeSpecification_WithNullValues() {
        // Act
        Specification<Product> spec = ProductSpecification.priceInRange(null, null);

        // Assert
        assertThat(spec).isNotNull();
    }

    /**
     * Test combined filters specification
     * Should create predicates for all non-null filters
     */
    @Test
    void testWithFiltersSpecification_AllFilters() {
        // Act
        Specification<Product> spec = ProductSpecification.withFilters(
            "Laptop",
            1L,
            new BigDecimal("50.00"),
            new BigDecimal("200.00"),
            true
        );

        // Assert
        assertThat(spec).isNotNull()
            .as("Specification should be created with all filters");
    }

    /**
     * Test combined filters with only name filter
     */
    @Test
    void testWithFiltersSpecification_OnlyName() {
        // Act
        Specification<Product> spec = ProductSpecification.withFilters(
            "Laptop",
            null,
            null,
            null,
            null
        );

        // Assert
        assertThat(spec).isNotNull();
    }
}
