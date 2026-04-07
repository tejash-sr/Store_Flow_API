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
 * Integration tests for CategoryRepository.
 * Tests CRUD operations and custom query methods.
 * 
 * Uses AbstractRepositoryTest which provides isolated PostgreSQL container.
 */
@Transactional
class CategoryRepositoryTest extends AbstractRepositoryTest {

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


}
