package com.storeflow.storeflow_api.repository;

import com.storeflow.storeflow_api.config.TestMailConfig;
import com.storeflow.storeflow_api.entity.Category;
import com.storeflow.storeflow_api.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ProductRepository.
 * Tests product data access with pricing and inventory queries.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@Transactional
class ProductRepositoryTest {

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

    @Test
    void testFindBySkuNotFound() {
        Optional<Product> found = productRepository.findBySku("NONEXISTENT");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByCategory() {
        productRepository.save(testProduct);
        Product mouse = Product.builder()
            .sku("MOUSE-001")
            .name("Wireless Mouse")
            .price(BigDecimal.valueOf(49.99))
            .category(electronics)
            .build();
        productRepository.save(mouse);

        List<Product> products = productRepository.findByCategory_IdAndIsActiveTrueOrderByNameAsc(electronics.getId());
        assertEquals(2, products.size());
    }

    @Test
    void testFindByPriceRange() {
        productRepository.save(testProduct);
        Product cheapProduct = Product.builder()
            .sku("CHEAP-001")
            .name("USB Cable")
            .price(BigDecimal.valueOf(10.00))
            .category(electronics)
            .build();
        productRepository.save(cheapProduct);

        List<Product> expensiveProducts = productRepository.findByPriceRange(
            BigDecimal.valueOf(500), BigDecimal.valueOf(2000)
        );
        assertTrue(expensiveProducts.stream().anyMatch(p -> "Dell XPS Laptop".equals(p.getName())));
    }

    @Test
    void testFindAllActiveProducts() {
        productRepository.save(testProduct);
        Product inactive = Product.builder()
            .sku("INACTIVE-001")
            .name("Discontinued Product")
            .price(BigDecimal.valueOf(100))
            .category(electronics)
            .isActive(false)
            .build();
        productRepository.save(inactive);

        List<Product> active = productRepository.findByIsActiveTrueOrderByNameAsc();
        assertTrue(active.stream().anyMatch(p -> "Dell XPS Laptop".equals(p.getName())));
        assertTrue(active.stream().noneMatch(p -> p.getIsActive().equals(false)));
    }

    @Test
    void testCountActiveProducts() {
        productRepository.save(testProduct);
        productRepository.save(Product.builder()
            .sku("MOUSE-002")
            .name("Gaming Mouse")
            .price(BigDecimal.valueOf(79.99))
            .category(electronics)
            .build());

        Long count = productRepository.countByIsActiveTrue();
        assertTrue(count >= 2);
    }

    @Test
    void testCalculateProfitMargin() {
        testProduct.setCost(BigDecimal.valueOf(800));
        testProduct.setPrice(BigDecimal.valueOf(1200));

        assertEquals(BigDecimal.valueOf(400), testProduct.getProfitMargin());
    }

    @Test
    void testCalculateProfitMarginPercentage() {
        testProduct.setCost(BigDecimal.valueOf(100));
        testProduct.setPrice(BigDecimal.valueOf(200));

        BigDecimal percentage = testProduct.getProfitMarginPercentage();
        assertEquals(0, percentage.compareTo(BigDecimal.valueOf(50)));
    }

    @Test
    void testExistsBySku() {
        productRepository.save(testProduct);
        assertTrue(productRepository.existsBySku("LAPTOP-001"));
        assertFalse(productRepository.existsBySku("NONEXISTENT"));
    }

    @Test
    void testSoftDeleteProduct() {
        Product saved = productRepository.save(testProduct);
        saved.softDelete();
        productRepository.save(saved);

        Optional<Product> found = productRepository.findBySku("LAPTOP-001");
        assertTrue(found.isPresent());
        assertFalse(found.get().getIsActive());
    }

    @Test
    void testProductRestoration() {
        Product saved = productRepository.save(testProduct);
        saved.softDelete();
        productRepository.save(saved);
        saved.restore();
        productRepository.save(saved);

        Optional<Product> found = productRepository.findBySku("LAPTOP-001");
        assertTrue(found.isPresent());
        assertTrue(found.get().getIsActive());
    }
}
