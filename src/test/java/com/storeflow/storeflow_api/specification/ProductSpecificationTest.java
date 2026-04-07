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
}

