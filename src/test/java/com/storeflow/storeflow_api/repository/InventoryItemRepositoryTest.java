package com.storeflow.storeflow_api.repository;

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

    @Test
    void testFindByProductId() {
        inventoryItemRepository.save(inventoryItem);
        List<InventoryItem> items = inventoryItemRepository
            .findByProduct_IdOrderByStore_NameAsc(product.getId());
        assertEquals(1, items.size());
    }

    @Test
    void testFindByStoreId() {
        inventoryItemRepository.save(inventoryItem);
        List<InventoryItem> items = inventoryItemRepository
            .findByStore_IdOrderByProduct_NameAsc(store.getId());
        assertTrue(items.size() >= 1);
    }

    @Test
    void testFindBelowMinimumStockLevel() {
        inventoryItem.setQuantityOnHand(10L); // Below minimum of 20
        inventoryItemRepository.save(inventoryItem);

        List<InventoryItem> lowStock = inventoryItemRepository.findBelowMinimumStockLevel();
        assertTrue(lowStock.stream()
            .anyMatch(item -> item.getId().equals(inventoryItem.getId())));
    }

    @Test
    void testIsBelowMinimum() {
        inventoryItem.setQuantityOnHand(5L);
        assertTrue(inventoryItem.isBelowMinimum());

        inventoryItem.setQuantityOnHand(20L);
        assertFalse(inventoryItem.isBelowMinimum());
    }

    @Test
    void testIncreaseQuantity() {
        inventoryItemRepository.save(inventoryItem);
        inventoryItem.increaseQuantity(50L);
        assertEquals(150L, inventoryItem.getQuantityOnHand());
    }

    @Test
    void testDecreaseQuantity() {
        inventoryItemRepository.save(inventoryItem);
        inventoryItem.decreaseQuantity(30L);
        assertEquals(70L, inventoryItem.getQuantityOnHand());
    }

    @Test
    void testDecreaseQuantityInsufficientStock() {
        inventoryItemRepository.save(inventoryItem);
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryItem.decreaseQuantity(200L);
        });
    }

    @Test
    void testHasStock() {
        assertTrue(inventoryItem.hasStock(50L));
        assertTrue(inventoryItem.hasStock(100L));
        assertFalse(inventoryItem.hasStock(150L));
    }

    @Test
    void testFindOutOfStock() {
        inventoryItem.setQuantityOnHand(0L);
        inventoryItemRepository.save(inventoryItem);

        List<InventoryItem> outOfStock = inventoryItemRepository.findOutOfStock();
        assertTrue(outOfStock.size() >= 1);
    }
}
