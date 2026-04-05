package com.storeflow.storeflow_api.specification;

import com.storeflow.storeflow_api.entity.Category;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.repository.CategoryRepository;
import com.storeflow.storeflow_api.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for ProductSpecification
 * Tests dynamic query specification construction for Products using real PostgreSQL database
 */
@DataJpaTest
@Testcontainers
class ProductSpecificationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category electronicsCategory;
    private Category clothingCategory;
    private Product laptop;
    private Product phone;
    private Product shirt;

    @BeforeEach
    void setUp() {
        // Create test categories
        electronicsCategory = categoryRepository.save(Category.builder()
                .name("Electronics")
                .isActive(true)
                .build());

        clothingCategory = categoryRepository.save(Category.builder()
                .name("Clothing")
                .isActive(true)
                .build());

        // Create test products
        laptop = productRepository.save(Product.builder()
                .name("Dell Laptop")
                .sku("DELL001")
                .price(new BigDecimal("999.99"))
                .category(electronicsCategory)
                .isActive(true)
                .build());

        phone = productRepository.save(Product.builder()
                .name("iPhone 15")
                .sku("IPHONE015")
                .price(new BigDecimal("799.99"))
                .category(electronicsCategory)
                .isActive(true)
                .build());

        shirt = productRepository.save(Product.builder()
                .name("Cotton Shirt")
                .sku("SHIRT001")
                .price(new BigDecimal("29.99"))
                .category(clothingCategory)
                .isActive(false)
                .build());

        entityManager.flush();
    }

    @Test
    void testNameContainsSpecification_WithValidName() {
        // Act: Search for products containing "Laptop"
        var results = productRepository.findAll(ProductSpecification.nameContains("Laptop"));

        // Assert: Should find only the laptop
        assertThat(results)
                .hasSize(1)
                .extracting("name")
                .contains("Dell Laptop");
    }

    @Test
    void testNameContainsSpecification_WithPartialMatch() {
        // Act: Search for products containing "i" (case-insensitive)
        var results = productRepository.findAll(ProductSpecification.nameContains("i"));

        // Assert: Should find iPhone and Shirt
        assertThat(results)
                .hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("iPhone 15", "Cotton Shirt");
    }

    @Test
    void testNameContainsSpecification_WithBlankName() {
        // Act: Search with empty string should match all
        var results = productRepository.findAll(ProductSpecification.nameContains(""));

        // Assert: Should return all products
        assertThat(results).hasSize(3);
    }

    @Test
    void testNameContainsSpecification_WithNullName() {
        // Act: Search with null name should match all
        var results = productRepository.findAll(ProductSpecification.nameContains(null));

        // Assert: Should return all products
        assertThat(results).hasSize(3);
    }

    @Test
    void testCategoryIdEqualsSpecification_WithValidCategoryId() {
        // Act: Filter by Electronics category
        var results = productRepository.findAll(ProductSpecification.categoryIdEquals(electronicsCategory.getId()));

        // Assert: Should find laptop and phone only
        assertThat(results)
                .hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("Dell Laptop", "iPhone 15");
    }

    @Test
    void testCategoryIdEqualsSpecification_WithDifferentCategory() {
        // Act: Filter by Clothing category
        var results = productRepository.findAll(ProductSpecification.categoryIdEquals(clothingCategory.getId()));

        // Assert: Should find shirt only
        assertThat(results)
                .hasSize(1)
                .extracting("name")
                .contains("Cotton Shirt");
    }

    @Test
    void testCategoryIdEqualsSpecification_WithNullCategoryId() {
        // Act: Search with null category should match all
        var results = productRepository.findAll(ProductSpecification.categoryIdEquals(null));

        // Assert: Should return all products
        assertThat(results).hasSize(3);
    }

    @Test
    void testPriceInRangeSpecification_WithValidRange() {
        // Act: Find products between 100 and 1000
        var results = productRepository.findAll(
                ProductSpecification.priceInRange(new BigDecimal("100.00"), new BigDecimal("1000.00")));

        // Assert: Should find laptop and phone
        assertThat(results)
                .hasSize(2)
                .extracting("price")
                .containsExactlyInAnyOrder(new BigDecimal("999.99"), new BigDecimal("799.99"));
    }

    @Test
    void testPriceInRangeSpecification_WithOnlyMinPrice() {
        // Act: Find products >= 500
        var results = productRepository.findAll(ProductSpecification.priceInRange(new BigDecimal("500.00"), null));

        // Assert: Should find laptop and phone
        assertThat(results)
                .hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("Dell Laptop", "iPhone 15");
    }

    @Test
    void testPriceInRangeSpecification_WithOnlyMaxPrice() {
        // Act: Find products <= 100
        var results = productRepository.findAll(ProductSpecification.priceInRange(null, new BigDecimal("100.00")));

        // Assert: Should find shirt only
        assertThat(results)
                .hasSize(1)
                .extracting("name")
                .contains("Cotton Shirt");
    }

    @Test
    void testPriceInRangeSpecification_WithNullPrices() {
        // Act: Search with null prices should match all
        var results = productRepository.findAll(ProductSpecification.priceInRange(null, null));

        // Assert: Should return all products
        assertThat(results).hasSize(3);
    }

    @Test
    void testIsActiveEqualsSpecification_WithTrue() {
        // Act: Find only active products
        var results = productRepository.findAll(ProductSpecification.isActiveEquals(true));

        // Assert: Should find laptop and phone only
        assertThat(results)
                .hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("Dell Laptop", "iPhone 15");
    }

    @Test
    void testIsActiveEqualsSpecification_WithFalse() {
        // Act: Find only inactive products
        var results = productRepository.findAll(ProductSpecification.isActiveEquals(false));

        // Assert: Should find shirt only
        assertThat(results)
                .hasSize(1)
                .extracting("name")
                .contains("Cotton Shirt");
    }

    @Test
    void testIsActiveEqualsSpecification_WithNull() {
        // Act: Search with null active status should match all
        var results = productRepository.findAll(ProductSpecification.isActiveEquals(null));

        // Assert: Should return all products
        assertThat(results).hasSize(3);
    }

    @Test
    void testIsActiveSpecification() {
        // Act: Find active products only
        var results = productRepository.findAll(ProductSpecification.isActive());

        // Assert: Should find only active products
        assertThat(results)
                .hasSize(2)
                .extracting("isActive")
                .allMatch(active -> active == true);
    }

    @Test
    void testWithFiltersSpecification_WithAllCriteria() {
        // Act: Filter with all criteria - Electronics, price 100-1000, name contains "i"
        var results = productRepository.findAll(
                ProductSpecification.withFilters("i", electronicsCategory.getId(),
                        new BigDecimal("100.00"), new BigDecimal("1000.00"), true));

        // Assert: Should find iPhone only (contains "i", Electronics, active, price in range)
        assertThat(results)
                .hasSize(1)
                .extracting("name")
                .contains("iPhone 15");
    }

    @Test
    void testWithFiltersSpecification_WithPartialCriteria() {
        // Act: Filter with only category and price
        var results = productRepository.findAll(
                ProductSpecification.withFilters(null, electronicsCategory.getId(), null, null, null));

        // Assert: Should find all electronics
        assertThat(results)
                .hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("Dell Laptop", "iPhone 15");
    }

    @Test
    void testWithFiltersSpecification_WithNameOnlyNotFound() {
        // Act: Search for non-existent product
        var results = productRepository.findAll(
                ProductSpecification.withFilters("NonExistent", null, null, null, null));

        // Assert: Should return empty
        assertThat(results).isEmpty();
    }

    @Test
    void testWithFiltersSpecification_WithPageable() {
        // Act: Apply specification with pagination
        Page<Product> results = productRepository.findAll(
                ProductSpecification.withFilters(null, electronicsCategory.getId(), null, null, null),
                PageRequest.of(0, 1));

        // Assert: Should return 1 result per page, 2 total
        assertThat(results)
                .hasSize(1);
        assertThat(results.getTotalElements()).isEqualTo(2);
        assertThat(results.getTotalPages()).isEqualTo(2);
    }
}

