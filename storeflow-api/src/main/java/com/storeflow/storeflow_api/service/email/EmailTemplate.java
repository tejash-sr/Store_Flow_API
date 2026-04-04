package com.storeflow.storeflow_api.service.email;

/**
 * Enumeration of email template types supported by the email service.
 * 
 * Each template type corresponds to a specific trigger event:
 * - WELCOME: User signup/registration
 * - PASSWORD_RESET: Forgot password request
 * - ORDER_CONFIRMED: New order created and confirmed
 * - LOW_STOCK_ALERT: Product inventory falls below threshold
 * - DAILY_DIGEST: Daily order summary for administrators
 * 
 * @author StoreFlow
 * @version 1.0
 */
public enum EmailTemplate {
    /**
     * Welcome email sent to new users after signup.
     * Contains: Welcome message, email verification link, account setup instructions
     */
    WELCOME("welcome", "Welcome to StoreFlow!"),

    /**
     * Password reset email sent when user requests password recovery.
     * Contains: Password reset link (time-limited token), expiry notice
     */
    PASSWORD_RESET("password-reset", "Reset Your Password"),

    /**
     * Order confirmation email sent when order status changes to CONFIRMED.
     * Contains: Order number, itemized list, total price, delivery address
     */
    ORDER_CONFIRMED("order-confirmed", "Order Confirmed"),

    /**
     * Low-stock alert sent to administrators when inventory drops below threshold.
     * Contains: Product name, current quantity, warehouse location, reorder recommendation
     */
    LOW_STOCK_ALERT("low-stock-alert", "Low Stock Alert"),

    /**
     * Daily digest email sent to administrators (scheduled or manual trigger).
     * Contains: Summary of orders placed that day, revenue, top products, pending orders
     */
    DAILY_DIGEST("daily-digest", "Daily Order Digest");

    /**
     * Template identifier for file lookup.
     */
    private final String templateId;

    /**
     * email subject line.
     */
    private final String subject;

    EmailTemplate(String templateId, String subject) {
        this.templateId = templateId;
        this.subject = subject;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getSubject() {
        return subject;
    }
}
