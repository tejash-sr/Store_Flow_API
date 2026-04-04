package com.storeflow.storeflow_api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.mock;

/**
 * Test configuration that provides a mock JavaMailSender bean for testing.
 * This prevents the ApplicationContext from failing due to missing email configuration.
 */
@TestConfiguration
public class TestMailConfig {

    /**
     * Provides a mock JavaMailSender bean for tests.
     * This avoids the need for actual SMTP server configuration in the test environment.
     *
     * @return Mock JavaMailSender
     */
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }
}
