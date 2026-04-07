package com.storeflow.storeflow_api;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.storeflow.storeflow_api.config.TestMailConfig;

/**
 * Abstract base class for repository integration tests.
 * 
 * Provides isolated PostgreSQL container via Testcontainers for each test class.
 * Uses @DataJpaTest for lightweight database layer testing without full Spring context.
 * 
 * Benefits:
 * - Real PostgreSQL database (matches production environment)
 * - Automatic rollback after each test (via @Transactional)
 * - No full app context overhead (@DataJpaTest is much faster than @SpringBootTest)
 * - Isolated - each test gets clean database via container
 * 
 * Usage:
 * ```java
 * class ProductRepositoryTest extends AbstractRepositoryTest {
 *     @Autowired
 *     private ProductRepository productRepository;
 *     
 *     @Test
 *     void testFindProduct() {
 *         productRepository.findById(1L);
 *     }
 * }
 * ```
 * 
 * @author StoreFlow
 * @version 1.0
 */
@DataJpaTest
@Testcontainers
@Import(TestMailConfig.class)
public abstract class AbstractRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("storeflow_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
