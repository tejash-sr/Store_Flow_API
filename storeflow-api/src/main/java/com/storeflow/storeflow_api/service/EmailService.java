package com.storeflow.storeflow_api.service;

/**
 * Email service interface for sending transactional emails.
 * Implementations send real emails via SMTP (can be mocked in tests).
 */
public interface EmailService {

    /**
     * Send password reset email with reset token link.
     * @param userEmail User's email address
     * @param fullName User's full name
     * @param resetToken Password reset token to include in link
     */
    void sendPasswordResetEmail(String userEmail, String fullName, String resetToken);

    /**
     * Send account created welcome/confirmation email.
     * @param userEmail User's email address
     * @param fullName User's full name
     */
    void sendWelcomeEmail(String userEmail, String fullName);

    /**
     * Send password changed confirmation email.
     * @param userEmail User's email address
     * @param fullName User's full name
     */
    void sendPasswordChangedEmail(String userEmail, String fullName);

    /**
     * Send order confirmation email immediately after order placement.
     * @param userEmail User's email address
     * @param fullName User's full name
     * @param orderNumber Order reference number
     */
    void sendOrderConfirmationEmail(String userEmail, String fullName, String orderNumber);

    /**
     * Send order status update email when order status changes.
     * @param userEmail User's email address
     * @param fullName User's full name
     * @param orderNumber Order reference number
     * @param newStatus New order status (PROCESSING, SHIPPED, DELIVERED, etc)
     */
    void sendOrderStatusUpdateEmail(String userEmail, String fullName, String orderNumber, String newStatus);
}
