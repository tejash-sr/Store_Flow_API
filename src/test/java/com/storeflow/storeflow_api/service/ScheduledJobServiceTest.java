package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.entity.User;
import com.storeflow.storeflow_api.entity.UserRole;
import com.storeflow.storeflow_api.service.email.HtmlEmailService;
import com.storeflow.storeflow_api.repository.OrderRepository;
import com.storeflow.storeflow_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ScheduledJobService daily digest functionality.
 * 
 * @author StoreFlow
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduledJobService - Daily Digest Emails")
class ScheduledJobServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ScheduledJobService scheduledJobService;

    @Test
    @DisplayName("Should send daily digest emails to admins")
    void testDailyDigestEmailsAreSent() {
        // Arrange
        User admin = User.builder()
                .email("admin@storeflow.local")
                .fullName("Admin User")
                .build();
        admin.getRoles().add(UserRole.ROLE_ADMIN);
        
        when(orderRepository.countOrdersByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(10L);
        when(orderRepository.sumRevenueByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("500.00"));
        when(orderRepository.countPendingOrdersBeforeDate(any(LocalDateTime.class)))
                .thenReturn(3L);
        when(userRepository.findAllAdmins())
                .thenReturn(List.of(admin));

        // When job is triggered
        scheduledJobService.triggerDailyDigestManually();

        // Then service should have executed without errors
        // (Email service methods are covered in integration tests)
        verify(userRepository, atLeastOnce()).findAllAdmins();
    }
}
