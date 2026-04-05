package com.storeflow.storeflow_api.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for sending transactional and marketing emails.
 * 
 * Supports 5 email templates:
 * 1. Welcome email (signup confirmation)
 * 2. Password reset email (forgot password)
 * 3. Order confirmation email (order placed)
 * 4. Low-stock alert email (inventory warning)
 * 5. Daily digest email (admin summary)
 * 
 * All emails are sent asynchronously using @Async.
 * In tests, Greenmail SMTP server is used for verification.
 * In production, configured SMTP provider (AWS SES, SendGrid, etc.) is used.
 * 
 * @author StoreFlow
 * @version 1.0
 */
@Slf4j
@Service("htmlEmailService")
@RequiredArgsConstructor
public class HtmlEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@storeflow.local}")
    private String fromEmail;

    @Value("${app.email.from-name:StoreFlow}")
    private String fromName;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Send welcome email to new user after signup.
     * 
     * Contains:
     * - Welcome greeting with user's name
     * - Email verification link
     * - Account setup instructions
     * - Link to app
     * 
     * @param toEmail recipient email address
     * @param fullName recipient's full name
     * @param verificationLink email verification link (time-limited)
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String fullName, String verificationLink) {
        try {
            if (toEmail == null || toEmail.trim().isEmpty()) {
                log.warn("Welcome email: recipient email is null/empty, skipping");
                return;
            }
            String htmlContent = buildWelcomeEmailHtml(fullName, verificationLink);
            sendHtmlEmail(toEmail, EmailTemplate.WELCOME, htmlContent);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * Send password reset email when user requests password recovery.
     * 
     * Contains:
     * - Password reset link
     * - Link expiry time (usually 24 hours)
     * - Security notice
     * - Instructions to contact support if not requested
     * 
     * @param toEmail recipient email address (the one requesting reset)
     * @param resetLink password reset link with time-limited token
     * @param expiryMinutes how many minutes the link is valid
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String resetLink, int expiryMinutes) {
        try {
            if (toEmail == null || toEmail.trim().isEmpty()) {
                log.warn("Password reset email: recipient email is null/empty, skipping");
                return;
            }
            String htmlContent = buildPasswordResetEmailHtml(resetLink, expiryMinutes);
            sendHtmlEmail(toEmail, EmailTemplate.PASSWORD_RESET, htmlContent);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * Send order confirmation email when order status is marked CONFIRMED.
     * 
     * Contains:
     * - Order number and date/time
     * - Itemized product list with quantities and prices
     * - Subtotal, tax, and total amount
     * - Delivery address
     * - Estimated delivery date
     * - Order tracking link
     * 
     * @param toEmail recipient email (customer)
     * @param customerName customer's name
     * @param orderNumber unique order ID
     * @param items list of ordered items (product name, qty, price)
     * @param totalAmount total order amount
     * @param deliveryAddress shipping address
     */
    @Async
    public void sendOrderConfirmationEmail(String toEmail, String customerName, String orderNumber,
                                          List<OrderItem> items, String totalAmount, String deliveryAddress) {
        try {
            if (toEmail == null || toEmail.trim().isEmpty()) {
                log.warn("Order confirmation email: recipient email is null/empty, skipping");
                return;
            }
            String htmlContent = buildOrderConfirmationEmailHtml(
                customerName, orderNumber, items, totalAmount, deliveryAddress
            );
            sendHtmlEmail(toEmail, EmailTemplate.ORDER_CONFIRMED, htmlContent);
            log.info("Order confirmation email sent to: {} for order: {}", toEmail, orderNumber);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * Send low-stock alert email to admins when inventory drops below threshold.
     * 
     * Contains:
     * - Product name and SKU
     * - Current quantity and minimum threshold
     * - Store/warehouse location
     * - Suggested reorder quantity
     * - Link to inventory management page
     * 
     * @param toEmail admin email address
     * @param productName product name
     * @param currentQty current quantity on hand
     * @param minimumLevel minimum stock threshold
     * @param warehouseLocation where the item is stored
     */
    @Async
    public void sendLowStockAlertEmail(String toEmail, String productName, Long currentQty,
                                      Long minimumLevel, String warehouseLocation) {
        try {
            if (toEmail == null || toEmail.trim().isEmpty()) {
                log.warn("Low-stock alert email: recipient email is null/empty, skipping");
                return;
            }
            String htmlContent = buildLowStockAlertEmailHtml(
                productName, currentQty, minimumLevel, warehouseLocation
            );
            sendHtmlEmail(toEmail, EmailTemplate.LOW_STOCK_ALERT, htmlContent);
            log.info("Low-stock alert email sent to: {} for product: {}", toEmail, productName);
        } catch (Exception e) {
            log.error("Failed to send low-stock alert email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * Send daily digest email to admins summarizing previous day's orders.
     * 
     * Contains:
     * - Total orders placed
     * - Total revenue
     * - Average order value
     * - Top 5 bestselling products
     * - Pending orders needing attention
     * - Low-stock items
     * 
     * Typically sent at specific time (e.g., 9 AM) via @Scheduled job.
     * Can also be triggered manually by admin.
     * 
     * @param toEmail admin email address
     * @param totalOrders count of orders placed
     * @param totalRevenue revenue for the day
     * @param avgOrderValue average order amount
     * @param pendingOrderCount orders awaiting action
     */
    @Async
    public void sendDailyDigestEmail(String toEmail, int totalOrders, String totalRevenue,
                                    String avgOrderValue, int pendingOrderCount) {
        try {
            if (toEmail == null || toEmail.trim().isEmpty()) {
                log.warn("Daily digest email: recipient email is null/empty, skipping");
                return;
            }
            String htmlContent = buildDailyDigestEmailHtml(
                totalOrders, totalRevenue, avgOrderValue, pendingOrderCount
            );
            sendHtmlEmail(toEmail, EmailTemplate.DAILY_DIGEST, htmlContent);
            log.info("Daily digest email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send daily digest email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * Generic method to send HTML email.
     * 
     * @param toEmail recipient email address
     * @param template email template (subject line, etc.)
     * @param htmlContent HTML body of the email
     * @throws MessagingException if mail sending fails
     * @throws UnsupportedEncodingException if encoding fails
     */
    private void sendHtmlEmail(String toEmail, EmailTemplate template, String htmlContent)
            throws MessagingException, UnsupportedEncodingException {
        
        // Validate inputs
        if (toEmail == null || toEmail.trim().isEmpty()) {
            log.warn("Email recipient is null or empty, skipping email send");
            return;
        }
        
        if (htmlContent == null) {
            log.warn("Email content is null for recipient: {}", toEmail);
            return;
        }
        
        // Create MIME message
        MimeMessage message = mailSender.createMimeMessage();
        
        // Check if message creation succeeded (can be null if JavaMailSender is not configured)
        if (message == null) {
            log.warn("Failed to create MimeMessage - JavaMailSender may not be properly configured. " +
                    "Email to {} will not be sent.", toEmail);
            return;
        }
        
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(template.getSubject());
            helper.setText(htmlContent, true); // true = isHtml

            message.saveChanges();

            mailSender.send(message);
        } catch (IllegalArgumentException e) {
            // Catch cases where helper methods receive null values
            log.error("Invalid email parameters for recipient {}: {}", toEmail, e.getMessage(), e);
            throw e;
        }
    }

    // ============ Email Template HTML Builders ============

    private String buildWelcomeEmailHtml(String fullName, String verificationLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; border-radius: 5px; }
                    .content { padding: 20px; background-color: #f9f9f9; margin: 20px 0; border-radius: 5px; }
                    .button { display: inline-block; background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to StoreFlow!</h1>
                    </div>
                    <div class="content">
                        <p>Hi <strong>%s</strong>,</p>
                        <p>Thank you for signing up! We're excited to have you on board.</p>
                        <p>To get started, please verify your email address by clicking the link below:</p>
                        <p><a href="%s" class="button">Verify Email</a></p>
                        <p>If the button doesn't work, copy and paste this link into your browser:<br>
                        <a href="%s">%s</a></p>
                        <p>Welcome aboard!<br><strong>The StoreFlow Team</strong></p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2026 StoreFlow. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, fullName, verificationLink, verificationLink, verificationLink);
    }

    private String buildPasswordResetEmailHtml(String resetLink, int expiryMinutes) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #ff9800; color: white; padding: 20px; text-align: center; border-radius: 5px; }
                    .content { padding: 20px; background-color: #f9f9f9; margin: 20px 0; border-radius: 5px; }
                    .button { display: inline-block; background-color: #d9534f; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; }
                    .warning { color: #ff6b6b; font-weight: bold; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Password Reset Request</h1>
                    </div>
                    <div class="content">
                        <p>We received a request to reset your password.</p>
                        <p>Click the link below to create a new password:</p>
                        <p><a href="%s" class="button">Reset Password</a></p>
                        <p>Or copy this link: <br><a href="%s">%s</a></p>
                        <p><span class="warning">⚠️ This link expires in %d minutes.</span></p>
                        <p>If you didn't request this, please ignore this email or contact support.</p>
                        <p>The StoreFlow Team</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2026 StoreFlow. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, resetLink, resetLink, resetLink, expiryMinutes);
    }

    private String buildOrderConfirmationEmailHtml(String customerName, String orderNumber,
                                                   List<OrderItem> items, String totalAmount,
                                                   String deliveryAddress) {
        StringBuilder itemsHtml = new StringBuilder();
        for (OrderItem item : items) {
            itemsHtml.append(String.format("""
                <tr>
                    <td style="padding: 10px; border-bottom: 1px solid #ddd;">%s</td>
                    <td style="padding: 10px; border-bottom: 1px solid #ddd; text-align: center;">%d</td>
                    <td style="padding: 10px; border-bottom: 1px solid #ddd; text-align: right;">$%s</td>
                </tr>
                """, item.productName(), item.quantity(), item.price()));
        }

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #28a745; color: white; padding: 20px; text-align: center; border-radius: 5px; }
                    .content { padding: 20px; background-color: #f9f9f9; margin: 20px 0; border-radius: 5px; }
                    table { width: 100%%; border-collapse: collapse; }
                    .total { font-weight: bold; font-size: 18px; text-align: right; padding: 15px 10px; background-color: #e9ecef; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Order Confirmed!</h1>
                    </div>
                    <div class="content">
                        <p>Hi <strong>%s</strong>,</p>
                        <p>Thank you for your order! Your order has been confirmed and will be processed shortly.</p>
                        <p><strong>Order Number:</strong> %s</p>
                        <p><strong>Order Date:</strong> %s</p>
                        <h3>Order Items:</h3>
                        <table>
                            <thead>
                                <tr style="background-color: #e9ecef;">
                                    <th style="padding: 10px; text-align: left;">Product</th>
                                    <th style="padding: 10px; text-align: center;">Quantity</th>
                                    <th style="padding: 10px; text-align: right;">Price</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>
                        <div class="total">Total: %s</div>
                        <p><strong>Delivery Address:</strong><br>%s</p>
                        <p>You can track your order status in your account dashboard.</p>
                        <p>Thank you for shopping with StoreFlow!<br><strong>The StoreFlow Team</strong></p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2026 StoreFlow. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, customerName, orderNumber, LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
            itemsHtml, totalAmount, deliveryAddress);
    }

    private String buildLowStockAlertEmailHtml(String productName, Long currentQty,
                                              Long minimumLevel, String warehouseLocation) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #ff9800; color: white; padding: 20px; text-align: center; border-radius: 5px; }
                    .alert-box { background-color: #fff3cd; border-left: 4px solid #ff9800; padding: 15px; margin: 20px 0; }
                    .info { padding: 10px; background-color: #f0f0f0; margin: 10px 0; border-radius: 3px; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ Low Stock Alert</h1>
                    </div>
                    <div class="alert-box">
                        <p><strong>ATTENTION:</strong> Inventory for the following product has dropped below the minimum threshold.</p>
                    </div>
                    <div class="content">
                        <div class="info">
                            <p><strong>Product:</strong> %s</p>
                            <p><strong>Current Quantity:</strong> %d units</p>
                            <p><strong>Minimum Level:</strong> %d units</p>
                            <p><strong>Location:</strong> %s</p>
                        </div>
                        <p><strong>Recommended Action:</strong> Review inventory levels and consider placing a reorder.</p>
                        <p>Log in to the admin dashboard to manage reorder quantities.</p>
                        <p>The StoreFlow Team</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2026 StoreFlow. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, productName, currentQty, minimumLevel, warehouseLocation);
    }

    private String buildDailyDigestEmailHtml(int totalOrders, String totalRevenue,
                                            String avgOrderValue, int pendingOrderCount) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; border-radius: 5px; }
                    .metric { display: inline-block; width: 48%%; margin: 1%%; padding: 15px; background-color: #f0f0f0; border-radius: 5px; text-align: center; }
                    .metric h3 { margin: 0 0 5px 0; color: #555; }
                    .metric .value { font-size: 24px; font-weight: bold; color: #007bff; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Daily Order Digest</h1>
                        <p>%s</p>
                    </div>
                    <div class="content">
                        <h2>Summary</h2>
                        <div class="metric">
                            <h3>Total Orders</h3>
                            <div class="value">%d</div>
                        </div>
                        <div class="metric">
                            <h3>Total Revenue</h3>
                            <div class="value">$%s</div>
                        </div>
                        <div class="metric">
                            <h3>Avg Order Value</h3>
                            <div class="value">$%s</div>
                        </div>
                        <div class="metric">
                            <h3>Pending Orders</h3>
                            <div class="value">%d</div>
                        </div>
                        <p>For more details, log in to the admin dashboard.</p>
                        <p>The StoreFlow Team</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2026 StoreFlow. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
            totalOrders, totalRevenue, avgOrderValue, pendingOrderCount);
    }

    /**
     * Helper record for order item details in confirmation email.
     */
    public record OrderItem(String productName, int quantity, String price) {}
}
