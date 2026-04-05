package com.storeflow.storeflow_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * AsyncJob entity - tracks the status of async background jobs
 * 
 * Allows clients to:
 * 1. Submit async job (e.g., "generate PDF")
 * 2. Receive job ID immediately
 * 3. Poll job status via GET /api/jobs/{jobId}
 * 4. Download result when complete
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "async_jobs", indexes = {
        @Index(name = "idx_job_user_id", columnList = "user_id"),
        @Index(name = "idx_job_status", columnList = "status"),
        @Index(name = "idx_job_type", columnList = "job_type")
})
public class AsyncJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Unique job identifier - uuid or similar for external reference
     */
    @Column(nullable = false, unique = true)
    private String jobId;
    
    /**
     * Type of job: PDF_REPORT, CSV_EXPORT, EMAIL_BATCH, etc.
     */
    @Column(nullable = false)
    private String jobType;
    
    /**
     * User who submitted the job
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Job status: PENDING, PROCESSING, COMPLETED, FAILED
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.PENDING;
    
    /**
     * Progress percentage (0-100)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer progress = 0;
    
    /**
     * Result data - JSON string with result details
     * For PDF: { "filePath": "/uploads/reports/abc123.pdf", "fileName": "order-report.pdf" }
     * Nullable until job completes
     */
    @Column(columnDefinition = "TEXT")
    private String resultData;
    
    /**
     * Error message - populated if job fails
     * Nullable if job succeeds
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * Estimated time to completion in seconds
     */
    private Integer estimatedSecondsRemaining;
    
    /**
     * Job submission timestamp
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Last status update timestamp
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Job completion timestamp
     * Set when status becomes COMPLETED or FAILED
     */
    private LocalDateTime completedAt;
    
    // Convenience methods
    
    public boolean isCompleted() {
        return status == JobStatus.COMPLETED || status == JobStatus.FAILED;
    }
    
    public boolean isSuccessful() {
        return status == JobStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return status == JobStatus.FAILED;
    }
    
    /**
     * Job status enum
     */
    public enum JobStatus {
        PENDING,      // Job created, waiting to start
        PROCESSING,   // Job actively running
        COMPLETED,    // Job finished successfully
        FAILED,       // Job failed with error
        CANCELLED     // Job cancelled by user
    }
}
