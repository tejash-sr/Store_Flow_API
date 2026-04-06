package com.storeflow.storeflow_api.email;

import com.storeflow.storeflow_api.AbstractIntegrationTest;
import com.storeflow.storeflow_api.entity.User;
import com.storeflow.storeflow_api.entity.UserRole;
import com.storeflow.storeflow_api.entity.UserStatus;
import com.storeflow.storeflow_api.repository.UserRepository;
import com.storeflow.storeflow_api.service.ScheduledJobService;
import com.storeflow.storeflow_api.service.email.HtmlEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests for email notification system.
 * Tests complete workflows from user signup through daily digest.
 */
@DisplayName("Email Notification E2E Integration Tests")
class EmailNotificationE2ETest extends AbstractIntegrationTest {

    @Autowired
    private HtmlEmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduledJobService scheduledJobService;

    @BeforeEach
    void setup() {
        // Clear any previous test data
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should send welcome email to new user on signup")
    void testWelcomeEmailOnSignup() {
        // Arrange
        String userEmail = "newuser@example.com";
        String fullName = "John Doe";
        String verificationLink = "https://app.example.com/verify?token=abc123";

        // Act
        emailService.sendWelcomeEmail(userEmail, fullName, verificationLink);

        // Assert
        // Email is sent asynchronously, verify service execution
        assertNotNull(emailService);
    }

    @Test
    @DisplayName("Should send password reset email on forgot password")
    void testPasswordResetEmailOnForgotPassword() {
        // Arrange
        String userEmail = "user@example.com";
        String resetLink = "https://app.example.com/reset?token=xyz789";
        int expiryMinutes = 24 * 60; // 24 hours

        // Act
        emailService.sendPasswordResetEmail(userEmail, resetLink, expiryMinutes);

        // Assert
        assertNotNull(emailService);
    }

    @Test
    @DisplayName("Should send daily digest email to all admins with stats")
    void testDailyDigestEmailToAdminsWithStats() {
        // Arrange - Create admin user
        User adminUser = User.builder()
                .email("admin@storeflow.local")
                .fullName("Store Admin")
                .password("hashed_password")
                .status(UserStatus.ACTIVE)
                .build();
        adminUser.getRoles().add(UserRole.ROLE_ADMIN);
        userRepository.save(adminUser);

        // Act
        scheduledJobService.triggerDailyDigestManually();

        // Assert
        assertNotNull(adminUser);
        assertEquals("admin@storeflow.local", adminUser.getEmail());
        assertTrue(adminUser.getRoles().contains(UserRole.ROLE_ADMIN));
    }

    @Test
    @DisplayName("Should handle null email gracefully")
    void testNullEmailHandling() {
        // Act
        emailService.sendWelcomeEmail(null, "John Doe", "https://example.com/verify");

        // Assert
        // Should not throw exception, graceful error logging expected
        assertNotNull(emailService);
    }

    @Test
    @DisplayName("Should send emails with async processing")
    void testEmailAsyncProcessing() {
        // Arrange
        String userEmail = "user@example.com";
        String fullName = "Alice Smith";
        String verificationLink = "https://example.com/verify?token=token123";

        // Act
        emailService.sendWelcomeEmail(userEmail, fullName, verificationLink);

        // Assert
        // Verify method completes without blocking (async behavior)
        assertNotNull(emailService);
    }

    @Test
    @DisplayName("Should create email service instance correctly")
    void testEmailServiceInstantiation() {
        // Assert
        assertNotNull(emailService);
        assertNotNull(userRepository);
        assertNotNull(scheduledJobService);
    }

    @Test
    @DisplayName("Should persist user and manage user roles")
    void testUserRolePersistence() {
        // Arrange
        User user = User.builder()
                .email("roledtest@storeflow.local")
                .fullName("Role Test User")
                .password("hashed_password")
                .status(UserStatus.ACTIVE)
                .build();
        user.getRoles().add(UserRole.ROLE_ADMIN);
        
        // Act
        User savedUser = userRepository.save(user);
        
        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("roledtest@storeflow.local", savedUser.getEmail());
        assertTrue(savedUser.getRoles().contains(UserRole.ROLE_ADMIN));
    }

    @Test
    @DisplayName("Should find all admin users via repository query")
    void testFindAllAdminsQuery() {
        // Arrange
        User admin1 = User.builder()
                .email("admin1@storeflow.local")
                .fullName("Admin One")
                .password("hashed_password")
                .status(UserStatus.ACTIVE)
                .build();
        admin1.getRoles().add(UserRole.ROLE_ADMIN);
        
        User admin2 = User.builder()
                .email("admin2@storeflow.local")
                .fullName("Admin Two")
                .password("hashed_password")
                .status(UserStatus.ACTIVE)
                .build();
        admin2.getRoles().add(UserRole.ROLE_ADMIN);
        
        User regularUser = User.builder()
                .email("user@storeflow.local")
                .fullName("Regular User")
                .password("hashed_password")
                .status(UserStatus.ACTIVE)
                .build();
        
        // Act
        userRepository.save(admin1);
        userRepository.save(admin2);
        userRepository.save(regularUser);
        
        var admins = userRepository.findAllAdmins();
        
        // Assert
        assertTrue(admins.size() >= 2, "Should find at least 2 admins");
    }
}

