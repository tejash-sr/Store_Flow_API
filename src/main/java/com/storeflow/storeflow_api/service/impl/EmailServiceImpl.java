package com.storeflow.storeflow_api.service.impl;

import com.storeflow.storeflow_api.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Real email service implementation using Spring's JavaMailSender.
 * Sends actual SMTP emails for all transactional notifications.
 * Can be mocked in tests with @MockBean to avoid real email sending.
 */
@Service("primaryEmailService")
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.from:noreply@storeflow.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void sendPasswordResetEmail(String userEmail, String fullName, String resetToken) {
        try {
            String resetLink = String.format("%s/reset-password?token=%s", frontendUrl, resetToken);
            String body = String.format(
                "Hi %s,\n\n" +
                "You requested to reset your password. Click the link below to set a new password:\n" +
                "%s\n\n" +
                "This link expires in 1 hour.\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Best regards,\nStoreFlow Team",
                fullName, resetLink);

            sendEmail(userEmail, "Password Reset Request", body);
            log.info("Password reset email sent to {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", userEmail, e.getMessage());
        }
    }

    @Override
    public void sendWelcomeEmail(String userEmail, String fullName) {
        try {
            String body = String.format(
                "Hi %s,\n\n" +
                "Welcome to StoreFlow! Your account has been successfully created.\n" +
                "You can now browse products, place orders, and manage your account.\n\n" +
                "Login here: %s\n\n" +
                "If you have any questions, please contact our support team.\n\n" +
                "Best regards,\nStoreFlow Team",
                fullName, frontendUrl);

            sendEmail(userEmail, "Welcome to StoreFlow", body);
            log.info("Welcome email sent to {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", userEmail, e.getMessage());
        }
    }

    @Override
    public void sendPasswordChangedEmail(String userEmail, String fullName) {
        try {
            String body = String.format(
                "Hi %s,\n\n" +
                "Your password has been successfully changed.\n" +
                "If you didn't make this change, please reset your password immediately.\n\n" +
                "Best regards,\nStoreFlow Team",
                fullName);

            sendEmail(userEmail, "Password Changed", body);
            log.info("Password changed email sent to {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send password changed email to {}: {}", userEmail, e.getMessage());
        }
    }

    @Override
    public void sendOrderConfirmationEmail(String userEmail, String fullName, String orderNumber) {
        try {
            String body = String.format(
                "Hi %s,\n\n" +
                "Thank you for your order!\n" +
                "Order Number: %s\n\n" +
                "We've received your order and will process it shortly.\n" +
                "You'll receive a shipping confirmation email soon.\n\n" +
                "Track your order: %s/orders/%s\n\n" +
                "Best regards,\nStoreFlow Team",
                fullName, orderNumber, frontendUrl, orderNumber);

            sendEmail(userEmail, "Order Confirmation - Order #" + orderNumber, body);
            log.info("Order confirmation email sent to {} for order {}", userEmail, orderNumber);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to {}: {}", userEmail, e.getMessage());
        }
    }

    @Override
    public void sendOrderStatusUpdateEmail(String userEmail, String fullName, String orderNumber, String newStatus) {
        try {
            String body = String.format(
                "Hi %s,\n\n" +
                "Your order status has been updated!\n" +
                "Order Number: %s\n" +
                "New Status: %s\n\n" +
                "Track your order: %s/orders/%s\n\n" +
                "Best regards,\nStoreFlow Team",
                fullName, orderNumber, newStatus, frontendUrl, orderNumber);

            sendEmail(userEmail, "Order Status Update - Order #" + orderNumber, body);
            log.info("Order status update email sent to {} for order {} (status: {})", userEmail, orderNumber, newStatus);
        } catch (Exception e) {
            log.error("Failed to send order status update email to {}: {}", userEmail, e.getMessage());
        }
    }

    /**
     * Send simple text email via SMTP.
     */
    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        javaMailSender.send(message);
        log.debug("Email sent to {} with subject: {}", to, subject);
    }
}
