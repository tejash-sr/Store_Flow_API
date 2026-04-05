package com.storeflow.storeflow_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import com.storeflow.storeflow_api.config.TestMailConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.datasource.driverClassName=org.h2.Driver",
	"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
	"spring.h2.console.enabled=false"
})
class StoreflowApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
