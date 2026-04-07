package com.storeflow.storeflow_api.repository;

import com.storeflow.AbstractRepositoryTest;
import com.storeflow.storeflow_api.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for InventoryItemRepository.
 * 
 * Uses AbstractRepositoryTest which provides isolated PostgreSQL container.
 */
@Transactional
class InventoryItemRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Store store;
    private Product product;
    private InventoryItem inventoryItem;

    @BeforeEach
    public void setUp() {
        Category cat = categoryRepository.save(Category.builder()
            .name("Test Admin")
            .isActive(true)
            .build());

        store = storeRepository.save(Store.builder()
            .storeCode("TEST-001")
            .name("Test Store")
            .address("123 Test St")
            .isActive(true)
            .build());

        product = productRepository.save(Product.builder()
            .sku("TEST-SKU")
            .name("Test Product")
            .price(BigDecimal.valueOf(99.99))
            .category(cat)
            .isActive(true)
            .build());

        inventoryItem = InventoryItem.builder()
            .product(product)
            .store(store)
            .quantityOnHand(100L)
            .minimumStockLevel(20L)
            .reorderQuantity(50L)
            .build();
    }

    @Test
    void testSaveInventoryItemAndRetrieve() {
        InventoryItem saved = inventoryItemRepository.save(inventoryItem);
        assertNotNull(saved.getId());
        assertEquals(100L, saved.getQuantityOnHand());
    }

    @Test
    void testFindByProductAndStore() {
        inventoryItemRepository.save(inventoryItem);
        Optional<InventoryItem> found = inventoryItemRepository
            .findByProduct_IdAndStore_Id(product.getId(), store.getId());
        assertTrue(found.isPresent());
        assertEquals(100L, found.get().getQuantityOnHand());
    }


}
