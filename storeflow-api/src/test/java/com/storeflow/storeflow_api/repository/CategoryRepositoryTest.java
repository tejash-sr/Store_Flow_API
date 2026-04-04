package com.storeflow.storeflow_api.repository;

import com.storeflow.storeflow_api.entity.Category;
import com.storeflow.storeflow_api.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CategoryRepository.
 * Tests CRUD operations and custom query methods.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Category testCategory;

    @BeforeEach
    public void setUp() {
        testCategory = Category.builder()
            .name("Electronics")
            .description("Electronic devices and accessories")
            .isActive(true)
            .build();
    }

    @Test
    void testSaveCategoryAndRetrieve() {
        Category saved = categoryRepository.save(testCategory);
        assertNotNull(saved.getId());
        assertEquals("Electronics", saved.getName());
    }

    @Test
    void testFindByNameAndIsActiveTrue() {
        categoryRepository.save(testCategory);
        Optional<Category> found = categoryRepository.findByNameAndIsActiveTrue("Electronics");
        assertTrue(found.isPresent());
        assertEquals("Electronics", found.get().getName());
    }

    @Test
    void testFindByNameNotFound() {
        Optional<Category> found = categoryRepository.findByNameAndIsActiveTrue("NonExistent");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAllActiveCategories() {
        categoryRepository.save(testCategory);
        Category inactive = Category.builder().name("Inactive").isActive(false).build();
        categoryRepository.save(inactive);

        List<Category> actives = categoryRepository.findByIsActiveTrueOrderByNameAsc();
        assertTrue(actives.stream().anyMatch(c -> "Electronics".equals(c.getName())));
        assertTrue(actives.stream().noneMatch(c -> c.getIsActive().equals(false)));
    }

    @Test
    void testCountActiveCategories() {
        categoryRepository.save(testCategory);
        categoryRepository.save(Category.builder().name("Books").isActive(true).build());

        Long count = categoryRepository.countByIsActiveTrue();
        assertTrue(count >= 2);
    }

    @Test
    void testSoftDelete() {
        Category saved = categoryRepository.save(testCategory);
        saved.softDelete();
        categoryRepository.save(saved);

        Optional<Category> found = categoryRepository.findByNameAndIsActiveTrue("Electronics");
        assertFalse(found.isPresent());
    }

    @Test
    void testAddProductToCategory() {
        Category saved = categoryRepository.save(testCategory);
        Product product = Product.builder()
            .sku("PROD001")
            .name("Laptop")
            .price(BigDecimal.valueOf(999.99))
            .category(saved)
            .isActive(true)
            .build();
        productRepository.save(product);

        Optional<Category> fetched = categoryRepository.findById(saved.getId());
        assertTrue(fetched.isPresent());
        assertTrue(fetched.get().getProducts().stream()
            .anyMatch(p -> "Laptop".equals(p.getName())));
    }

    @Test
    void testFindAllActiveWithProducts() {
        Category saved = categoryRepository.save(testCategory);
        Product product = Product.builder()
            .sku("PROD002")
            .name("Mouse")
            .price(BigDecimal.valueOf(29.99))
            .category(saved)
            .build();
        productRepository.save(product);

        List<Category> categories = categoryRepository.findAllActiveWithProducts();
        assertFalse(categories.isEmpty());
        assertTrue(categories.stream().anyMatch(c -> "Electronics".equals(c.getName())));
    }

    @Test
    void testCategoryWithMultipleProducts() {
        Category saved = categoryRepository.save(testCategory);
        for (int i = 0; i < 5; i++) {
            Product p = Product.builder()
                .sku("SKU-" + i)
                .name("Product " + i)
                .price(BigDecimal.valueOf(100 + i))
                .category(saved)
                .build();
            productRepository.save(p);
        }

        Optional<Category> fetched = categoryRepository.findById(saved.getId());
        assertTrue(fetched.isPresent());
        assertEquals(5, fetched.get().getProducts().size());
    }

    @Test
    void testExistsByNameAndIsActiveTrue() {
        categoryRepository.save(testCategory);
        assertTrue(categoryRepository.existsByNameAndIsActiveTrue("Electronics"));
        assertFalse(categoryRepository.existsByNameAndIsActiveTrue("NonExistent"));
    }
}
