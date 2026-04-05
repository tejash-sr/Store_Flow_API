package com.storeflow.storeflow_api.config;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test configuration that provides a simple JavaMailSender implementation for testing.
 * 
 * Instead of mocking, we provide a real implementation that captures messages to a list.
 * This allows tests to verify email content without requiring a real SMTP server.
 */
@TestConfiguration
public class TestMailConfig {

    // Thread-safe list to capture sent messages
    private static final List<MimeMessage> sentMessages = new CopyOnWriteArrayList<>();

    /**
     * Provides a simple JavaMailSender implementation that captures email messages.
     * Email content can be retrieved via getSentMessages() for assertions.
     */
    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {
            private final Session session = Session.getInstance(new Properties());

            @Override
            public MimeMessage createMimeMessage() {
                return new MimeMessage(session);
            }

            @Override
            public MimeMessage createMimeMessage(InputStream inputStream) throws MailException {
                try {
                    return new MimeMessage(session, inputStream);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create MIME message from inputStream", e);
                }
            }

            @Override
            public void send(MimeMessage mimeMessage) throws MailException {
                // Capture the message for testing
                sentMessages.add(mimeMessage);
            }

            @Override
            public void send(MimeMessage... mimeMessages) throws MailException {
                for (MimeMessage message : mimeMessages) {
                    send(message);
                }
            }

            @Override
            public void send(MimeMessagePreparator preparator) throws MailException {
                try {
                    MimeMessage message = createMimeMessage();
                    preparator.prepare(message);
                    send(message);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to send message", e);
                }
            }

            @Override
            public void send(MimeMessagePreparator... preparators) throws MailException {
                for (MimeMessagePreparator prep : preparators) {
                    send(prep);
                }
            }

            // SimpleMailMessage support (from MailSender interface)
            @Override
            public void send(SimpleMailMessage simpleMessage) throws MailException {
                // Not used in tests, no-op
            }

            @Override
            public void send(SimpleMailMessage... simpleMessages) throws MailException {
                // Not used in tests, no-op
            }
        };
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




