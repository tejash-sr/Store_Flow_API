package com.storeflow;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Abstract base class for repository integration tests.
 * 
 * Uses @DataJpaTest for lightweight JPA layer testing.
 * Tests use the PostgreSQL service configured in application-test.yml.
 * 
 * @author StoreFlow
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
public abstract class AbstractRepositoryTest {
    // Spring automatically provides JPA test configuration
    // No additional setup needed - uses application-test.yml PostgreSQL config
}

