package com.storeflow.storeflow_api.repository;

import com.storeflow.storeflow_api.entity.AsyncJob;
import com.storeflow.storeflow_api.entity.AsyncJob.JobStatus;
import com.storeflow.storeflow_api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AsyncJob Repository
 * Provides data access for async job tracking and polling
 */
@Repository
public interface AsyncJobRepository extends JpaRepository<AsyncJob, Long> {
    
    /**
     * Find job by unique jobId (external reference)
     */
    Optional<AsyncJob> findByJobId(String jobId);
    
    /**
     * Find all jobs for a specific user
     */
    Page<AsyncJob> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * Find all jobs for a user with specific status
     */
    Page<AsyncJob> findByUserAndStatusOrderByCreatedAtDesc(User user, JobStatus status, Pageable pageable);
    
    /**
     * Find all incomplete jobs (for monitoring dashboard)
     */
    @Query("SELECT j FROM AsyncJob j WHERE j.status IN ('PENDING', 'PROCESSING') ORDER BY j.createdAt DESC")
    List<AsyncJob> findAllIncompleteJobs();
    
    /**
     * Find all failed jobs
     */
    List<AsyncJob> findByStatusOrderByCreatedAtDesc(JobStatus status);
    
    /**
     * Find jobs older than specified date (for cleanup)
     */
    @Query("SELECT j FROM AsyncJob j WHERE j.completedAt < :date")
    List<AsyncJob> findJobsOlderThan(LocalDateTime date);
}
