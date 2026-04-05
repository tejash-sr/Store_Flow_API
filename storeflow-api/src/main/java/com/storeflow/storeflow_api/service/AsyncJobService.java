package com.storeflow.storeflow_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storeflow.storeflow_api.entity.AsyncJob;
import com.storeflow.storeflow_api.entity.AsyncJob.JobStatus;
import com.storeflow.storeflow_api.entity.User;
import com.storeflow.storeflow_api.repository.AsyncJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * AsyncJob Service
 * Manages creation, tracking, and polling of async background jobs
 * 
 * Workflow:
 * 1. Client calls endpoint that initiates async job
 * 2. Service creates AsyncJob entity with PENDING status and returns jobId
 * 3. Actual async processing starts in background
 * 4. Client polls GET /api/jobs/{jobId} for status
 * 5. When COMPLETED, resultData contains the result (e.g., file path)
 */
@Slf4j
@Service
@Transactional
public class AsyncJobService {
    
    private final AsyncJobRepository jobRepository;
    private final ObjectMapper objectMapper;
    
    public AsyncJobService(AsyncJobRepository jobRepository, ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Create a new async job
     * Called when user initiates an async operation
     * 
     * @param user user who initiated the job
     * @param jobType type of job (e.g., "PDF_REPORT", "CSV_EXPORT")
     * @return created AsyncJob with PENDING status
     */
    public AsyncJob createJob(User user, String jobType) {
        String jobId = UUID.randomUUID().toString();
        
        AsyncJob job = AsyncJob.builder()
                .jobId(jobId)
                .jobType(jobType)
                .user(user)
                .status(JobStatus.PENDING)
                .progress(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        AsyncJob savedJob = jobRepository.save(job);
        log.info("Created async job: jobId={}, type={}, user={}", jobId, jobType, user.getEmail());
        return savedJob;
    }
    
    /**
     * Get job by jobId
     * Called by client polling for job status
     * 
     * @param jobId unique job identifier
     * @return AsyncJob if found
     * @throws RuntimeException if job not found
     */
    public AsyncJob getJobById(String jobId) {
        return jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
    }
    
    /**
     * Mark job as processing
     * Call this when async task starts executing
     * 
     * @param job the async job
     */
    public void markProcessing(AsyncJob job) {
        job.setStatus(JobStatus.PROCESSING);
        job.setProgress(10);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);
        log.debug("Job marked as PROCESSING: {}", job.getJobId());
    }
    
    /**
     * Update job progress
     * Call periodically from async task to update progress bar
     * 
     * @param job the async job
     * @param progress progress percentage (0-100)
     * @param estimatedSecondsRemaining estimated time remaining
     */
    public void updateProgress(AsyncJob job, Integer progress, Integer estimatedSecondsRemaining) {
        job.setProgress(Math.min(progress, 99)); // Cap at 99 until completion
        job.setEstimatedSecondsRemaining(estimatedSecondsRemaining);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);
        log.debug("Job progress updated: jobId={}, progress={}%", job.getJobId(), progress);
    }
    
    /**
     * Mark job as completed with result data
     * Call when async task completes successfully
     * 
     * @param job the async job
     * @param resultData result as Map (will be serialized to JSON)
     */
    public void markCompleted(AsyncJob job, Map<String, Object> resultData) {
        try {
            String resultJson = objectMapper.writeValueAsString(resultData);
            job.setResultData(resultJson);
            job.setStatus(JobStatus.COMPLETED);
            job.setProgress(100);
            job.setCompletedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());
            job.setErrorMessage(null); // Clear any previous errors
            jobRepository.save(job);
            log.info("Job marked as COMPLETED: jobId={}, result={}", job.getJobId(), resultData);
        } catch (Exception e) {
            log.error("Error completing job {}: {}", job.getJobId(), e.getMessage());
            markFailed(job, "JSON serialization error: " + e.getMessage());
        }
    }
    
    /**
     * Mark job as failed with error message
     * Call when async task encounters error
     * 
     * @param job the async job
     * @param errorMessage error description
     */
    public void markFailed(AsyncJob job, String errorMessage) {
        job.setStatus(JobStatus.FAILED);
        job.setProgress(0);
        job.setErrorMessage(errorMessage);
        job.setCompletedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);
        log.error("Job marked as FAILED: jobId={}, error={}", job.getJobId(), errorMessage);
    }
    
    /**
     * Get all jobs for user with pagination
     * 
     * @param user user to fetch jobs for
     * @param page page number (0-based)
     * @param size page size
     * @return paginated jobs for user
     */
    public Page<AsyncJob> getUserJobs(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return jobRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    /**
     * Get jobs with specific status for user
     * 
     * @param user user to fetch jobs for
     * @param status job status filter
     * @param page page number
     * @param size page size
     * @return paginated jobs matching criteria
     */
    public Page<AsyncJob> getUserJobsByStatus(User user, JobStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return jobRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status, pageable);
    }
    
    /**
     * Clean up old completed jobs (for database maintenance)
     * 
     * @param ageInDays delete jobs completed more than X days ago
     * @return count of deleted jobs
     */
    public long cleanupOldJobs(int ageInDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(ageInDays);
        var oldJobs = jobRepository.findJobsOlderThan(cutoff);
        jobRepository.deleteAll(oldJobs);
        log.info("Cleaned up {} old async jobs", oldJobs.size());
        return oldJobs.size();
    }
}
