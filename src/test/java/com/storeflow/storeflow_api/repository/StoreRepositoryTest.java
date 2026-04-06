package com.storeflow.storeflow_api.repository;

import com.storeflow.storeflow_api.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.storeflow.storeflow_api.testsupport.AbstractRepositoryTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for StoreRepository.
 * Tests store data access with location queries.
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

    @Test
    void testFindAllActiveStores() {
        storeRepository.save(testStore);
        Store inactive = Store.builder()
            .storeCode("LA-CLOSED")
            .name("LA Closed Store")
            .address("456 Main St")
            .isActive(false)
            .build();
        storeRepository.save(inactive);

        List<Store> actives = storeRepository.findByIsActiveTrueOrderByNameAsc();
        assertTrue(actives.stream().anyMatch(s -> "NYC-001".equals(s.getStoreCode())));
    }

    @Test
    void testFindByCity() {
        storeRepository.save(testStore);
        Store chicago = Store.builder()
            .storeCode("CHI-001")
            .name("Chicago Store")
            .address("789 Michigan Ave")
            .city("Chicago")
            .state("IL")
            .isActive(true)
            .build();
        storeRepository.save(chicago);

        List<Store> nyStores = storeRepository.findByCityAndIsActiveTrueOrderByNameAsc("New York");
        assertTrue(nyStores.stream().anyMatch(s -> "NYC-001".equals(s.getStoreCode())));
    }

    @Test
    void testFindByState() {
        storeRepository.save(testStore);
        Store buffalo = Store.builder()
            .storeCode("BUF-001")
            .name("Buffalo Store")
            .address("999 Main St")
            .city("Buffalo")
            .state("NY")
            .isActive(true)
            .build();
        storeRepository.save(buffalo);

        List<Store> nyStores = storeRepository.findByStateAndIsActiveTrueOrderByNameAsc("NY");
        assertEquals(2, nyStores.size());
    }

    @Test
    void testCountActiveStores() {
        storeRepository.save(testStore);
        storeRepository.save(Store.builder()
            .storeCode("LA-001")
            .name("LA Store")
            .address("111 Hollywood Ave")
            .isActive(true)
            .build());

        Long count = storeRepository.countByIsActiveTrue();
        assertTrue(count >= 2);
    }

    @Test
    void testExistsByStoreCode() {
        storeRepository.save(testStore);
        assertTrue(storeRepository.existsByStoreCode("NYC-001"));
        assertFalse(storeRepository.existsByStoreCode("NONEXISTENT"));
    }

    @Test
    void testSoftDeleteStore() {
        Store saved = storeRepository.save(testStore);
        saved.softDelete();
        storeRepository.save(saved);

        Optional<Store> found = storeRepository.findByStoreCode("NYC-001");
        assertTrue(found.isPresent());
        assertFalse(found.get().getIsActive());
    }
}
