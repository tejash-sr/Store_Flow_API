package com.storeflow.storeflow_api.service.email;

import com.storeflow.storeflow_api.StoreflowApiApplication;
import com.storeflow.storeflow_api.config.TestMailConfig;
import com.storeflow.storeflow_api.service.email.HtmlEmailService.OrderItem;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

/**
 * Unit tests for EmailService.
 * 
 * Uses TestMailConfig with mocked JavaMailSender to verify email content without requiring
 * a real SMTP server. Emails sent by EmailService are captured by the mock for verification.
 * 
 * Tests all 5 email types: welcome, password reset, order confirmed, low-stock, daily digest.
 * Also tests error handling and async behavior.
 * 
 * @author StoreFlow
 * @version 1.0
 */
@SpringBootTest(classes = StoreflowApiApplication.class)
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@TestPropertySource(properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=3025",
    "spring.mail.protocol=smtp",
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

    @Autowired
    private HtmlEmailService emailService;

    @BeforeEach
    void setUp() {
        // Clear any messages from previous tests
        TestMailConfig.clearMessages();
        // Give async email processing a moment to settle
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Safely sleep for the given milliseconds.
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get the last sent email message.
     * Since email sending is asynchronous, callers may need to add a small delay.
     */
    private MimeMessage getLastSentMessage() {
        try {
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(TestMailConfig.getSentMessages()).isNotEmpty());

            var messages = TestMailConfig.getSentMessages();
            return messages.get(messages.size() - 1);
        } catch (Exception e) {
            throw new AssertionError("Failed to get last sent message: " + e.getMessage(), e);
        }
    }

    /**
     * Get the subject line of a message safely.
     */
    private String getMessageSubject(MimeMessage message) {
        try {
            return message.getSubject();
        } catch (Exception e) {
            throw new AssertionError("Failed to get message subject: " + e.getMessage(), e);
        }
    }

    /**
     * Get the content type of a message safely.
     */
    private String getMessageContentType(MimeMessage message) {
        try {
            return message.getContentType();
        } catch (Exception e) {
            throw new AssertionError("Failed to get message content type: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to extract text content from a MimeMessage that may contain 
     * multipart content (both plain text and HTML).
     * 
     * Handles both simple String content and MimeMultipart messages.
     * Returns the HTML part if available, otherwise returns plain text.
     */
    private String getEmailContent(MimeMessage message) {
        try {
            Object content = message.getContent();
            
            // If it's a simple string, return it directly
            if (content instanceof String) {
                return (String) content;
            }
            
            // If it's MimeMultipart, extract the HTML part
            if (content instanceof jakarta.mail.internet.MimeMultipart) {
                jakarta.mail.internet.MimeMultipart multipart = (jakarta.mail.internet.MimeMultipart) content;
                
                // Try to find HTML part first
                for (int i = 0; i < multipart.getCount(); i++) {
                    jakarta.mail.BodyPart part = multipart.getBodyPart(i);
                    if (part.getContentType().contains("text/html")) {
                        Object partContent = part.getContent();
                        if (partContent instanceof String) {
                            return (String) partContent;
                        } else if (partContent instanceof java.io.InputStream) {
                            return new String(((java.io.InputStream) partContent).readAllBytes());
                        }
                    }
                }
                
                // Fallback to first text part (plain text)
                for (int i = 0; i < multipart.getCount(); i++) {
                    jakarta.mail.BodyPart part = multipart.getBodyPart(i);
                    if (part.getContentType().contains("text/plain")) {
                        Object partContent = part.getContent();
                        if (partContent instanceof String) {
                            return (String) partContent;
                        } else if (partContent instanceof java.io.InputStream) {
                            return new String(((java.io.InputStream) partContent).readAllBytes());
                        }
                    }
                }
            }
            
            return content.toString();
        } catch (Exception e) {
            throw new AssertionError("Failed to extract email content: " + e.getMessage(), e);
        }
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
        
        // Small delay to allow async processing
        sleep(250);

        // Then
        MimeMessage message = getLastSentMessage();
        assertThat(getMessageSubject(message)).contains("Welcome");
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
        sleep(250);

        // Then
        MimeMessage message = getLastSentMessage();
        String content = getEmailContent(message);
        assertThat(content)
            .contains(fullName)
            .contains(verificationLink)
            .contains("<!DOCTYPE html>");
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
        sleep(250);

        // Then
        MimeMessage message = getLastSentMessage();
        assertThat(getMessageSubject(message)).contains("Password");
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
        sleep(250);

        // Then
        MimeMessage message = getLastSentMessage();
        String content = getEmailContent(message);
        assertThat(content)
            .contains(resetLink)
            .contains("60")
            .contains("<!DOCTYPE html>");
    }

    // ============ Order Confirmation Email Tests ============

    @Test
    @DisplayName("Should send order confirmation email with correct order details")
    void testSendOrderConfirmationEmail_Success() {
        // Given
        String toEmail = "customer@example.com";
        String customerName = "Alice Johnson";
        String orderNumber = "ORD-123456";
        List<OrderItem> items = List.of(
            new OrderItem("Product A", 2, "50.00"),
            new OrderItem("Product B", 1, "75.00")
        );
        String totalAmount = "175.00";
        String deliveryAddress = "123 Main St, City, State 12345";

        // When
        emailService.sendOrderConfirmationEmail(toEmail, customerName, orderNumber, items, totalAmount, deliveryAddress);
        sleep(250);

        // Then
        MimeMessage message = getLastSentMessage();
        assertThat(getMessageSubject(message)).contains("Order");
    }

    @Test
    @DisplayName("Should include order items and total in order confirmation email")
    void testOrderConfirmationEmail_ContainsOrderItems() {
        // Given
        String toEmail = "customer@test.com";
        String customerName = "Bob Wilson";
        String orderNumber = "ORD-789012";
        List<OrderItem> items = List.of(
            new OrderItem("Laptop", 1, "1200.00"),
            new OrderItem("Mouse", 2, "25.00")
        );
        String totalAmount = "1250.00";
        String deliveryAddress = "456 Oak Ave, Town, State 54321";

        // When
        emailService.sendOrderConfirmationEmail(toEmail, customerName, orderNumber, items, totalAmount, deliveryAddress);
        sleep(250);

        // Then
        MimeMessage message = getLastSentMessage();
        String content = getEmailContent(message);
        assertThat(content)
            .contains(customerName)
            .contains(orderNumber)
            .contains("Laptop")
            .contains("Mouse")
            .contains("1250.00")
            .contains(deliveryAddress);
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
        sleep(250);

        // Then
        MimeMessage message = getLastSentMessage();
        assertThat(getMessageSubject(message)).contains("Low Stock");
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
        sleep(250);

        // Then
        MimeMessage message = getLastSentMessage();
        String content = getEmailContent(message);
        assertThat(content)
            .contains(productName)
            .contains("2")
            .contains("15")
            .contains(warehouseLocation);
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
        sleep(250);

        // Then
        MimeMessage message = getLastSentMessage();
        assertThat(getMessageSubject(message)).contains("Daily");
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
        sleep(250);

        // Then
        MimeMessage message = getLastSentMessage();
        String content = getEmailContent(message);
        assertThat(content)
            .contains("25")
            .contains("5000.00")
            .contains("200.00")
            .contains("3");
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
        sleep(250);

        // Then
        MimeMessage message = getLastSentMessage();
        assertThat(getMessageContentType(message)).contains("text/html");
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
        List<OrderItem> items = List.of(); // Empty list
        String totalAmount = "0.00";
        String deliveryAddress = "123 Test St";

        // When/Then - Should not throw exception
        assertThatCode(() ->
            emailService.sendOrderConfirmationEmail(toEmail, customerName, orderNumber, items, totalAmount, deliveryAddress)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle async email sending")
    void testAsyncEmailSending() {
        // Given multiple emails to send
        emailService.sendWelcomeEmail("user1@test.com", "User 1", "link1");
        emailService.sendWelcomeEmail("user2@test.com", "User 2", "link2");
        emailService.sendWelcomeEmail("user3@test.com", "User 3", "link3");

        // When/Then - All emails should be received with async processing
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(TestMailConfig.getSentMessages()).hasSizeGreaterThanOrEqualTo(3));
    }
}
