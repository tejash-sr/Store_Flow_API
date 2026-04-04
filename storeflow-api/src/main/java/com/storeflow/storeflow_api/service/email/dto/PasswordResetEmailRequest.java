package com.storeflow.storeflow_api.service.email.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for sending password reset email.
 * 
 * Contains recipient email, reset link, and expiry duration.
 */
@Data
@Builder
public class PasswordResetEmailRequest {
    private String toEmail;
    private String resetLink;
    private int expiryMinutes;
}
