package com.storeflow.storeflow_api.repository;

import com.storeflow.AbstractRepositoryTest;
import com.storeflow.storeflow_api.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for StoreRepository.
 * Tests store data access with location queries.
 * 
 * Uses AbstractRepositoryTest which provides isolated PostgreSQL container.
 */
@Transactional
class StoreRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    private Store testStore;

    @BeforeEach
    public void setUp() {
        testStore = Store.builder()
            .storeCode("NYC-001")
            .name("New York Main Store")
            .address("123 5th Avenue")
            .city("New York")
            .state("NY")
            .zipCode("10001")
            .phoneNumber("2125551234")
            .email("nyc@storeflow.com")
            .isActive(true)
            .build();
    }

    @Test
    void testSaveStoreAndRetrieve() {
        Store saved = storeRepository.save(testStore);
        assertNotNull(saved.getId());
        assertEquals("NYC-001", saved.getStoreCode());
    }

    @Test
    void testFindByStoreCode() {
        storeRepository.save(testStore);
        Optional<Store> found = storeRepository.findByStoreCode("NYC-001");
        assertTrue(found.isPresent());
        assertEquals("New York Main Store", found.get().getName());
    }


}
