package com.storeflow.storeflow_api.service.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Unit tests for email services.
 * Tests HTML email composition and delivery.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Email Service Unit Tests")
public class EmailServiceTest {

    @Autowired(required = false)
    private HtmlEmailService htmlEmailService;

    @BeforeEach
    void setUp() {
        assertNotNull(htmlEmailService, "HtmlEmailService should be injected");
    }

    @Test
    @DisplayName("Should send welcome email successfully")
    void testSendWelcomeEmail() {
        String email = "user@test.com";
        String name = "Test User";
        String verificationLink = "http://localhost/verify/token123";

        assertDoesNotThrow(() -> {
            htmlEmailService.sendWelcomeEmail(email, name, verificationLink);
        });
    }

    @Test
    @DisplayName("Should send password reset email successfully")
    void testSendPasswordResetEmail() {
        String email = "user@test.com";
        String resetLink = "http://localhost/reset/token456";

        assertDoesNotThrow(() -> {
            htmlEmailService.sendPasswordResetEmail(email, resetLink, 60);
        });
    }
}
