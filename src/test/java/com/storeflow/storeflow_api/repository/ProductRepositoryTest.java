package com.storeflow.storeflow_api.repository;

import com.storeflow.AbstractRepositoryTest;
import com.storeflow.storeflow_api.entity.Category;
import com.storeflow.storeflow_api.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ProductRepository.
 * Tests product data access with pricing and inventory queries.
 * 
 * Uses AbstractRepositoryTest which provides isolated PostgreSQL container.
 */
@Transactional
class ProductRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category electronics;
    private Product testProduct;

    @BeforeEach
    public void setUp() {
        electronics = categoryRepository.save(
            Category.builder()
                .name("Electronics")
                .description("Electronic devices")
                .isActive(true)
                .build()
        );

        testProduct = Product.builder()
            .sku("LAPTOP-001")
            .name("Dell XPS Laptop")
            .description("High-performance laptop")
            .price(BigDecimal.valueOf(1299.99))
            .cost(BigDecimal.valueOf(999.99))
            .category(electronics)
            .isActive(true)
            .build();
    }

    @Test
    void testSaveProductAndRetrieve() {
        Product saved = productRepository.save(testProduct);
        assertNotNull(saved.getId());
        assertEquals("Dell XPS Laptop", saved.getName());
        assertEquals("LAPTOP-001", saved.getSku());
    }

    @Test
    void testFindBySku() {
        productRepository.save(testProduct);
        Optional<Product> found = productRepository.findBySku("LAPTOP-001");
        assertTrue(found.isPresent());
        assertEquals("Dell XPS Laptop", found.get().getName());
    }


}
