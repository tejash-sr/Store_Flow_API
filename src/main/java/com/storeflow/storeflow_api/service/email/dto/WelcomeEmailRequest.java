package com.storeflow.storeflow_api.service.email.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for sending welcome email.
 * 
 * Contains recipient email, name, and verification link.
 */
@Data
@Builder
public class WelcomeEmailRequest {
    private String toEmail;
    private String fullName;
    private String verificationLink;
}
