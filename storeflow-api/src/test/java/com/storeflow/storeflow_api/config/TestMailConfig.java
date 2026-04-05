package com.storeflow.storeflow_api.config;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test configuration that provides a mock JavaMailSender bean for testing.
 * 
 * Captures all sent email messages in a thread-safe list for verification in tests.
 * This allows tests to verify email content without requiring a real SMTP server.
 * 
 * NOTE: This is NOT a @Configuration class - it must be explicitly imported
 * using @Import(TestMailConfig.class) to be activated.
 */
public class TestMailConfig {

    // Thread-safe list to capture sent messages
    private static final List<MimeMessage> sentMessages = new CopyOnWriteArrayList<>();

    /**
     * Provides a mock JavaMailSender that captures email messages.
     * Email content can be retrieved via getSentMessages() for assertions.
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        
        // When createMimeMessage is called, return a mock MimeMessage
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        // Capture sent messages for test verification
        doAnswer(invocation -> {
            MimeMessage message = invocation.getArgument(0);
            sentMessages.add(message);
            return null;
        }).when(mailSender).send(any(MimeMessage.class));
        
        // Also handle MimeMessagePreparator
        doAnswer(invocation -> {
            MimeMessagePreparator preparator = invocation.getArgument(0);
            MimeMessage message = mock(MimeMessage.class);
            preparator.prepare(message);
            sentMessages.add(message);
            return null;
        }).when(mailSender).send(any(MimeMessagePreparator.class));
        
        return mailSender;
    }

    /**
     * Get all messages sent during this test.
     * Useful for assertions on email content.
     */
    public static List<MimeMessage> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }

    /**
     * Clear all captured messages.
     * Call this in @BeforeEach to reset state between tests.
     */
    public static void clearMessages() {
        sentMessages.clear();
    }
}



