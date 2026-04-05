package com.storeflow.storeflow_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.storeflow.storeflow_api.service.email.EmailService;
import com.storeflow.storeflow_api.repository.OrderRepository;
import com.storeflow.storeflow_api.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Scheduled job service for automated tasks like daily email digests.
 * 
 * Jobs:
 * 1. sendDailyDigestEmails() - Every day at 9:00 AM
 *    - Collects statistics for previous day
 *    - Sends digest email to all admins
 *    - Includes: total orders, revenue, average order value, pending orders
 * 
 * @author StoreFlow
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledJobService {

    private final EmailService emailService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    /**
     * Send daily digest emails to admins at 9:00 AM every day.
     * Cron: "0 0 9 * * *" = 09:00:00 every day (seconds, minutes, hours, day, month, day-of-week)
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyDigestEmails() {
        try {
            log.info("Starting daily digest email job");

            // Calculate statistics for previous day
            LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIDNIGHT);
            LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);

            // Get daily statistics from order data
            long totalOrdersCount = orderRepository.countOrdersByDateRange(startOfDay, endOfDay);
            int totalOrders = (int) totalOrdersCount;
            
            BigDecimal totalRevenue = orderRepository.sumRevenueByDateRange(startOfDay, endOfDay);
            if (totalRevenue == null) {
                totalRevenue = BigDecimal.ZERO;
            }
            
            String totalRevenueStr = totalRevenue.toPlainString();
            
            BigDecimal averageOrderValue = totalOrders > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            
            String avgOrderValueStr = averageOrderValue.toPlainString();
            
            long pendingOrdersCount = orderRepository.countPendingOrdersBeforeDate(LocalDateTime.now());
            int pendingOrders = (int) pendingOrdersCount;

            log.info("Daily stats - Orders: {}, Revenue: {}, Average: {}, Pending: {}",
                totalOrders, totalRevenue, averageOrderValue, pendingOrders);

            // Send to all admins
            userRepository.findAllAdmins().forEach(admin -> {
                try {
                    emailService.sendDailyDigestEmail(
                        admin.getEmail(),
                        totalOrders,
                        totalRevenueStr,
                        avgOrderValueStr,
                        pendingOrders
                    );
                    log.debug("Daily digest email sent to admin: {}", admin.getEmail());
                } catch (Exception e) {
                    log.error("Failed to send digest email to: {}", admin.getEmail(), e);
                }
            });

            log.info("Daily digest email job completed successfully");
        } catch (Exception e) {
            log.error("Error in daily digest email job", e);
        }
    }

    /**
     * Optional: Test endpoint to trigger digest job manually.
     * Can be called from API for testing or admin requests.
     */
    public void triggerDailyDigestManually() {
        log.info("Manual trigger: Daily digest job");
        sendDailyDigestEmails();
    }
}
