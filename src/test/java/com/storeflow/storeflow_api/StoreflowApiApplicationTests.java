package com.storeflow.storeflow_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.storeflow.storeflow_api.config.TestMailConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class StoreflowApiApplicationTests {

	@Test
	void contextLoads() {
		// Verifies that Spring context loads with Testcontainers PostgreSQL
		// Flyway migrations are validated, JPA is wired, Security is configured
	}

}
