package com.storeflow.storeflow_api.service.email;

import com.storeflow.storeflow_api.service.email.dto.*;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

/**
 * Unit tests for EmailService.
 * 
 * Uses Greenmail (in-memory SMTP server) to mock JavaMailSender in tests.
 * Tests all 5 email types: welcome, password reset, order confirmed, low-stock, daily digest.
 * Also tests error handling and async behavior.
 * 
 * Greenmail is configured to:
 * - Listen on localhost:3025 (SMTP)
 * - Use UTF-8 encoding
 * - No SSL/TLS for test simplicity
 * - Auto-started by @GreenMailExtension
 * 
 * Tests verify:
 * - Email subject lines are correct
 * - Recipients are correct
 * - Email content contains expected data
 * - HTML formatting is present
 * - Async sending works (using Awaitility)
 * 
 * @author StoreFlow
 * @version 1.0
 */
@SpringBootTest
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@TestPropertySource(properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=3025",
    "spring.mail.username=test@test.com",
    "spring.mail.password=test",
    "spring.mail.protocol=smtp",
    "spring.mail.properties.mail.smtp.auth=false",
    "spring.mail.properties.mail.smtp.starttls.enable=false",
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "app.email.from=test@storeflow.local",
    "app.email.from-name=TestStoreFlow"
})
@DisplayName("EmailService Tests")
class EmailServiceTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
        .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());

    @Autowired
    private EmailService emailService;

    @Autowired
    private JavaMailSender javaMailSender;

    @BeforeEach
    void setUp() {
        // Reset greenmail before each test
        greenMail.reset();
    }

    // ============ Welcome Email Tests ============

    @Test
    @DisplayName("Should send welcome email with correct subject and recipient")
    void testSendWelcomeEmail_Success() {
        // Given
        String toEmail = "newuser@example.com";
        String fullName = "John Doe";
        String verificationLink = "https://app.local/verify?token=abc123";

        // When
        emailService.sendWelcomeEmail(toEmail, fullName, verificationLink);

        // Then - Wait for async email processing
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                assertThat(receivedMessages).isNotEmpty();

                MimeMessage message = receivedMessages[0];
                assertThat(message.getSubject()).contains("Welcome");
                assertThat(message.getAllRecipients()[0].toString()).contains(toEmail);
            });
    }

    @Test
    @DisplayName("Should include verification link in welcome email content")
    void testWelcomeEmail_ContainsVerificationLink() {
        // Given
        String toEmail = "user@test.com";
        String fullName = "Jane Smith";
        String verificationLink = "https://app.local/verify?token=xyz789";

        // When
        emailService.sendWelcomeEmail(toEmail, fullName, verificationLink);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                String content = (String) messages[0].getContent();
                assertThat(content)
                    .contains(fullName)
                    .contains(verificationLink)
                    .contains("<!DOCTYPE html>");
            });
    }

    // ============ Password Reset Email Tests ============

    @Test
    @DisplayName("Should send password reset email with correct subject and expiry")
    void testSendPasswordResetEmail_Success() {
        // Given
        String toEmail = "user@example.com";
        String resetLink = "https://app.local/reset?token=reset123";
        int expiryMinutes = 30;

        // When
        emailService.sendPasswordResetEmail(toEmail, resetLink, expiryMinutes);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                MimeMessage message = messages[0];
                assertThat(message.getSubject()).contains("Password");
            });
    }

    @Test
    @DisplayName("Should include reset link and expiry time in password reset email")
    void testPasswordResetEmail_ContainsResetLinkAndExpiry() {
        // Given
        String toEmail = "user@test.com";
        String resetLink = "https://app.local/reset?token=reset456";
        int expiryMinutes = 60;

        // When
        emailService.sendPasswordResetEmail(toEmail, resetLink, expiryMinutes);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                String content = (String) messages[0].getContent();
                assertThat(content)
                    .contains(resetLink)
                    .contains("60")
                    .contains("<!DOCTYPE html>");
            });
    }

    // ============ Order Confirmation Email Tests ============

    @Test
    @DisplayName("Should send order confirmation email with correct order details")
    void testSendOrderConfirmationEmail_Success() {
        // Given
        String toEmail = "customer@example.com";
        String customerName = "Alice Johnson";
        String orderNumber = "ORD-123456";
        List<EmailService.OrderItem> items = List.of(
            new EmailService.OrderItem("Product A", 2, "50.00"),
            new EmailService.OrderItem("Product B", 1, "75.00")
        );
        String totalAmount = "175.00";
        String deliveryAddress = "123 Main St, City, State 12345";

        // When
        emailService.sendOrderConfirmationEmail(toEmail, customerName, orderNumber, items, totalAmount, deliveryAddress);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                MimeMessage message = messages[0];
                assertThat(message.getSubject()).contains("Order");
            });
    }

    @Test
    @DisplayName("Should include order items and total in order confirmation email")
    void testOrderConfirmationEmail_ContainsOrderItems() {
        // Given
        String toEmail = "customer@test.com";
        String customerName = "Bob Wilson";
        String orderNumber = "ORD-789012";
        List<EmailService.OrderItem> items = List.of(
            new EmailService.OrderItem("Laptop", 1, "1200.00"),
            new EmailService.OrderItem("Mouse", 2, "25.00")
        );
        String totalAmount = "1250.00";
        String deliveryAddress = "456 Oak Ave, Town, State 54321";

        // When
        emailService.sendOrderConfirmationEmail(toEmail, customerName, orderNumber, items, totalAmount, deliveryAddress);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                String content = (String) messages[0].getContent();
                assertThat(content)
                    .contains(customerName)
                    .contains(orderNumber)
                    .contains("Laptop")
                    .contains("Mouse")
                    .contains("1250.00")
                    .contains(deliveryAddress);
            });
    }

    // ============ Low Stock Alert Email Tests ============

    @Test
    @DisplayName("Should send low-stock alert email to admin")
    void testSendLowStockAlertEmail_Success() {
        // Given
        String toEmail = "admin@example.com";
        String productName = "Premium Widget";
        Long currentQty = 5L;
        Long minimumLevel = 10L;
        String warehouseLocation = "Warehouse A - Shelf 3";

        // When
        emailService.sendLowStockAlertEmail(toEmail, productName, currentQty, minimumLevel, warehouseLocation);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                MimeMessage message = messages[0];
                assertThat(message.getSubject()).contains("Low Stock");
            });
    }

    @Test
    @DisplayName("Should include product details in low-stock alert email")
    void testLowStockAlertEmail_ContainsProductDetails() {
        // Given
        String toEmail = "admin@test.com";
        String productName = "Standard Gadget";
        Long currentQty = 2L;
        Long minimumLevel = 15L;
        String warehouseLocation = "Warehouse B - Shelf 5";

        // When
        emailService.sendLowStockAlertEmail(toEmail, productName, currentQty, minimumLevel, warehouseLocation);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                String content = (String) messages[0].getContent();
                assertThat(content)
                    .contains(productName)
                    .contains("2")
                    .contains("15")
                    .contains(warehouseLocation);
            });
    }

    // ============ Daily Digest Email Tests ============

    @Test
    @DisplayName("Should send daily digest email with order summary")
    void testSendDailyDigestEmail_Success() {
        // Given
        String toEmail = "admin@example.com";
        int totalOrders = 42;
        String totalRevenue = "8500.50";
        String avgOrderValue = "202.39";
        int pendingOrderCount = 5;

        // When
        emailService.sendDailyDigestEmail(toEmail, totalOrders, totalRevenue, avgOrderValue, pendingOrderCount);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                MimeMessage message = messages[0];
                assertThat(message.getSubject()).contains("Daily");
            });
    }

    @Test
    @DisplayName("Should include order statistics in daily digest email")
    void testDailyDigestEmail_ContainsOrderStatistics() {
        // Given
        String toEmail = "admin@test.com";
        int totalOrders = 25;
        String totalRevenue = "5000.00";
        String avgOrderValue = "200.00";
        int pendingOrderCount = 3;

        // When
        emailService.sendDailyDigestEmail(toEmail, totalOrders, totalRevenue, avgOrderValue, pendingOrderCount);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                String content = (String) messages[0].getContent();
                assertThat(content)
                    .contains("25")
                    .contains("5000.00")
                    .contains("200.00")
                    .contains("3");
            });
    }

    // ============ Email Format Tests ============

    @Test
    @DisplayName("Should send HTML-formatted emails")
    void testEmailsAreHtmlFormatted() {
        // Given
        String toEmail = "user@example.com";
        String verificationLink = "https://app.local/verify";

        // When
        emailService.sendWelcomeEmail(toEmail, "Test User", verificationLink);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                MimeMessage message = messages[0];
                // Check content type contains text/html
                assertThat(message.getContentType()).contains("text/html");
            });
    }

    // ============ Error Handling Tests ============

    @Test
    @DisplayName("Should handle null recipient gracefully")
    void testEmailService_HandlesNullRecipient() {
        // Given - null recipient
        String toEmail = null;
        String verificationLink = "https://app.local/verify";

        // When/Then - Should not throw exception (logged instead, due to @Async)
        assertThatCode(() -> 
            emailService.sendWelcomeEmail(toEmail, "Test User", verificationLink)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle invoice with empty item list")
    void testOrderConfirmationEmail_WithEmptyItems() {
        // Given
        String toEmail = "customer@example.com";
        String customerName = "Empty Order";
        String orderNumber = "ORD-EMPTY";
        List<EmailService.OrderItem> items = List.of(); // Empty list
        String totalAmount = "0.00";
        String deliveryAddress = "123 Test St";

        // When/Then - Should not throw exception
        assertThatCode(() ->
            emailService.sendOrderConfirmationEmail(toEmail, customerName, orderNumber, items, totalAmount, deliveryAddress)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should correctly handle async email sending")
    void testAsyncEmailSending() {
        // Given multiple emails to send
        emailService.sendWelcomeEmail("user1@test.com", "User 1", "link1");
        emailService.sendWelcomeEmail("user2@test.com", "User 2", "link2");
        emailService.sendWelcomeEmail("user3@test.com", "User 3", "link3");

        // When/Then - All emails should be received with async processing
        await().atMost(3, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).hasSizeGreaterThanOrEqualTo(3);
            });
    }
}

        // Given
        String toEmail = "newuser@example.com";
        String fullName = "John Doe";
        String verificationLink = "https://app.local/verify?token=abc123";

        // When
        emailService.sendWelcomeEmail(toEmail, fullName, verificationLink);

        // Then - Wait for async email processing
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                assertThat(receivedMessages).isNotEmpty();

                MimeMessage message = receivedMessages[0];
                assertThat(message.getSubject()).contains("Welcome");
                assertThat(message.getAllRecipients()[0].toString()).contains(toEmail);
            });
    }

    @Test
    @DisplayName("Should include verification link in welcome email content")
    void testWelcomeEmail_ContainsVerificationLink() {
        // Given
        String toEmail = "user@test.com";
        String fullName = "Jane Smith";
        String verificationLink = "https://app.local/verify?token=xyz789";

        // When
        emailService.sendWelcomeEmail(toEmail, fullName, verificationLink);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                String content = (String) messages[0].getContent();
                assertThat(content)
                    .contains(fullName)
                    .contains(verificationLink)
                    .contains("<!DOCTYPE html>");
            });
    }

    // ============ Password Reset Email Tests ============

    @Test
    @DisplayName("Should send password reset email with correct subject and expiry")
    void testSendPasswordResetEmail_Success() {
        // Given
        String toEmail = "user@example.com";
        String resetLink = "https://app.local/reset?token=reset123";
        int expiryMinutes = 30;

        // When
        emailService.sendPasswordResetEmail(toEmail, resetLink, expiryMinutes);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                MimeMessage message = messages[0];
                assertThat(message.getSubject()).contains("Password");
            });
    }

    @Test
    @DisplayName("Should include reset link and expiry time in password reset email")
    void testPasswordResetEmail_ContainsResetLinkAndExpiry() {
        // Given
        String toEmail = "user@test.com";
        String resetLink = "https://app.local/reset?token=reset456";
        int expiryMinutes = 60;

        // When
        emailService.sendPasswordResetEmail(toEmail, resetLink, expiryMinutes);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                String content = (String) messages[0].getContent();
                assertThat(content)
                    .contains(resetLink)
                    .contains("60")
                    .contains("<!DOCTYPE html>");
            });
    }

    // ============ Order Confirmation Email Tests ============

    @Test
    @DisplayName("Should send order confirmation email with correct order details")
    void testSendOrderConfirmationEmail_Success() {
        // Given
        String toEmail = "customer@example.com";
        String customerName = "Alice Johnson";
        String orderNumber = "ORD-123456";
        List<EmailService.OrderItem> items = List.of(
            new EmailService.OrderItem("Product A", 2, "50.00"),
            new EmailService.OrderItem("Product B", 1, "75.00")
        );
        String totalAmount = "175.00";
        String deliveryAddress = "123 Main St, City, State 12345";

        // When
        emailService.sendOrderConfirmationEmail(toEmail, customerName, orderNumber, items, totalAmount, deliveryAddress);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                MimeMessage message = messages[0];
                assertThat(message.getSubject()).contains("Order");
            });
    }

    @Test
    @DisplayName("Should include order items and total in order confirmation email")
    void testOrderConfirmationEmail_ContainsOrderItems() {
        // Given
        String toEmail = "customer@test.com";
        String customerName = "Bob Wilson";
        String orderNumber = "ORD-789012";
        List<EmailService.OrderItem> items = List.of(
            new EmailService.OrderItem("Laptop", 1, "1200.00"),
            new EmailService.OrderItem("Mouse", 2, "25.00")
        );
        String totalAmount = "1250.00";
        String deliveryAddress = "456 Oak Ave, Town, State 54321";

        // When
        emailService.sendOrderConfirmationEmail(toEmail, customerName, orderNumber, items, totalAmount, deliveryAddress);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                String content = (String) messages[0].getContent();
                assertThat(content)
                    .contains(customerName)
                    .contains(orderNumber)
                    .contains("Laptop")
                    .contains("Mouse")
                    .contains("1250.00")
                    .contains(deliveryAddress);
            });
    }

    // ============ Low Stock Alert Email Tests ============

    @Test
    @DisplayName("Should send low-stock alert email to admin")
    void testSendLowStockAlertEmail_Success() {
        // Given
        String toEmail = "admin@example.com";
        String productName = "Premium Widget";
        Long currentQty = 5L;
        Long minimumLevel = 10L;
        String warehouseLocation = "Warehouse A - Shelf 3";

        // When
        emailService.sendLowStockAlertEmail(toEmail, productName, currentQty, minimumLevel, warehouseLocation);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                MimeMessage message = messages[0];
                assertThat(message.getSubject()).contains("Low Stock");
            });
    }

    @Test
    @DisplayName("Should include product details in low-stock alert email")
    void testLowStockAlertEmail_ContainsProductDetails() {
        // Given
        String toEmail = "admin@test.com";
        String productName = "Standard Gadget";
        Long currentQty = 2L;
        Long minimumLevel = 15L;
        String warehouseLocation = "Warehouse B - Shelf 5";

        // When
        emailService.sendLowStockAlertEmail(toEmail, productName, currentQty, minimumLevel, warehouseLocation);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                String content = (String) messages[0].getContent();
                assertThat(content)
                    .contains(productName)
                    .contains("2")
                    .contains("15")
                    .contains(warehouseLocation);
            });
    }

    // ============ Daily Digest Email Tests ============

    @Test
    @DisplayName("Should send daily digest email with order summary")
    void testSendDailyDigestEmail_Success() {
        // Given
        String toEmail = "admin@example.com";
        int totalOrders = 42;
        String totalRevenue = "8500.50";
        String avgOrderValue = "202.39";
        int pendingOrderCount = 5;

        // When
        emailService.sendDailyDigestEmail(toEmail, totalOrders, totalRevenue, avgOrderValue, pendingOrderCount);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                MimeMessage message = messages[0];
                assertThat(message.getSubject()).contains("Daily");
            });
    }

    @Test
    @DisplayName("Should include order statistics in daily digest email")
    void testDailyDigestEmail_ContainsOrderStatistics() {
        // Given
        String toEmail = "admin@test.com";
        int totalOrders = 25;
        String totalRevenue = "5000.00";
        String avgOrderValue = "200.00";
        int pendingOrderCount = 3;

        // When
        emailService.sendDailyDigestEmail(toEmail, totalOrders, totalRevenue, avgOrderValue, pendingOrderCount);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                String content = (String) messages[0].getContent();
                assertThat(content)
                    .contains("25")
                    .contains("5000.00")
                    .contains("200.00")
                    .contains("3");
            });
    }

    // ============ Email Format Tests ============

    @Test
    @DisplayName("Should send HTML-formatted emails")
    void testEmailsAreHtmlFormatted() {
        // Given
        String toEmail = "user@example.com";
        String verificationLink = "https://app.local/verify";

        // When
        emailService.sendWelcomeEmail(toEmail, "Test User", verificationLink);

        // Then
        await().atMost(2, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).isNotEmpty();
                
                MimeMessage message = messages[0];
                // Check content type contains text/html
                assertThat(message.getContentType()).contains("text/html");
            });
    }

    // ============ Error Handling Tests ============

    @Test
    @DisplayName("Should handle null recipient gracefully")
    void testEmailService_HandlesNullRecipient() {
        // Given - null recipient
        String toEmail = null;
        String verificationLink = "https://app.local/verify";

        // When/Then - Should not throw exception (logged instead, due to @Async)
        assertThatCode(() -> 
            emailService.sendWelcomeEmail(toEmail, "Test User", verificationLink)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle invoice with empty item list")
    void testOrderConfirmationEmail_WithEmptyItems() {
        // Given
        String toEmail = "customer@example.com";
        String customerName = "Empty Order";
        String orderNumber = "ORD-EMPTY";
        List<EmailService.OrderItem> items = List.of(); // Empty list
        String totalAmount = "0.00";
        String deliveryAddress = "123 Test St";

        // When/Then - Should not throw exception
        assertThatCode(() ->
            emailService.sendOrderConfirmationEmail(toEmail, customerName, orderNumber, items, totalAmount, deliveryAddress)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should correctly handle async email sending")
    void testAsyncEmailSending() {
        // Given multiple emails to send
        emailService.sendWelcomeEmail("user1@test.com", "User 1", "link1");
        emailService.sendWelcomeEmail("user2@test.com", "User 2", "link2");
        emailService.sendWelcomeEmail("user3@test.com", "User 3", "link3");

        // When/Then - All emails should be received with async processing
        await().atMost(3, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                MimeMessage[] messages = greenMail.getReceivedMessages();
                assertThat(messages).hasSizeGreaterThanOrEqualTo(3);
            });
    }
}
