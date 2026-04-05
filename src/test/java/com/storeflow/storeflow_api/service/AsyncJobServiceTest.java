package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.entity.AsyncJob;
import com.storeflow.storeflow_api.entity.AsyncJob.JobStatus;
import com.storeflow.storeflow_api.entity.User;
import com.storeflow.storeflow_api.repository.AsyncJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AsyncJobService Unit Tests
 * Tests async job lifecycle: creation, progress tracking, completion, failure
 */
@ExtendWith(MockitoExtension.class)
class AsyncJobServiceTest {
    
    @Mock
    private AsyncJobRepository jobRepository;
    
    @InjectMocks
    private AsyncJobService asyncJobService;
    
    private User testUser;
    private AsyncJob testJob;
    
    @BeforeEach
    void setUp() {
        asyncJobService = new AsyncJobService(jobRepository, new ObjectMapper());
        
        testUser = new User();
        testUser.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        
        testJob = new AsyncJob();
        testJob.setId(1L);
        testJob.setJobId("job-123-abc");
        testJob.setJobType("PDF_REPORT");
        testJob.setUser(testUser);
        testJob.setStatus(JobStatus.PENDING);
        testJob.setProgress(0);
    }
    
    @Test
    void testCreateJob_CreatesNewJobWithPendingStatus() {
        // Arrange
        when(jobRepository.save(any(AsyncJob.class))).thenReturn(testJob);
        
        // Act
        AsyncJob createdJob = asyncJobService.createJob(testUser, "PDF_REPORT");
        
        // Assert
        ArgumentCaptor<AsyncJob> jobCaptor = ArgumentCaptor.forClass(AsyncJob.class);
        verify(jobRepository).save(jobCaptor.capture());
        
        AsyncJob capturedJob = jobCaptor.getValue();
        assertThat(capturedJob.getUser()).isEqualTo(testUser);
        assertThat(capturedJob.getJobType()).isEqualTo("PDF_REPORT");
        assertThat(capturedJob.getStatus()).isEqualTo(JobStatus.PENDING);
        assertThat(capturedJob.getProgress()).isEqualTo(0);
        assertThat(capturedJob.getJobId()).isNotNull();
    }
    
    @Test
    void testGetJobById_ReturnsJobWhenFound() {
        // Arrange
        when(jobRepository.findByJobId("job-123-abc")).thenReturn(Optional.of(testJob));
        
        // Act
        AsyncJob found = asyncJobService.getJobById("job-123-abc");
        
        // Assert
        assertThat(found).isEqualTo(testJob);
        verify(jobRepository).findByJobId("job-123-abc");
    }
    
    @Test
    void testGetJobById_ThrowsExceptionWhenNotFound() {
        // Arrange
        when(jobRepository.findByJobId("nonexistent")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> asyncJobService.getJobById("nonexistent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Job not found");
    }
    
    @Test
    void testMarkProcessing_UpdatesStatusToProcessing() {
        // Arrange
        when(jobRepository.save(any(AsyncJob.class))).thenReturn(testJob);
        testJob.setStatus(JobStatus.PENDING);
        
        // Act
        asyncJobService.markProcessing(testJob);
        
        // Assert
        ArgumentCaptor<AsyncJob> jobCaptor = ArgumentCaptor.forClass(AsyncJob.class);
        verify(jobRepository).save(jobCaptor.capture());
        
        AsyncJob captured = jobCaptor.getValue();
        assertThat(captured.getStatus()).isEqualTo(JobStatus.PROCESSING);
        assertThat(captured.getProgress()).isEqualTo(10);
    }
    
    @Test
    void testUpdateProgress_IncrementsProgressAndEstimate() {
        // Arrange
        when(jobRepository.save(any(AsyncJob.class))).thenReturn(testJob);
        
        // Act
        asyncJobService.updateProgress(testJob, 50, 30);
        
        // Assert
        ArgumentCaptor<AsyncJob> jobCaptor = ArgumentCaptor.forClass(AsyncJob.class);
        verify(jobRepository).save(jobCaptor.capture());
        
        AsyncJob captured = jobCaptor.getValue();
        assertThat(captured.getProgress()).isEqualTo(50);
        assertThat(captured.getEstimatedSecondsRemaining()).isEqualTo(30);
    }
    
    @Test
    void testMarkCompleted_SetsResultDataAndStatus() {
        // Arrange
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("filePath", "/uploads/report.pdf");
        resultData.put("fileName", "report.pdf");
        
        when(jobRepository.save(any(AsyncJob.class))).thenReturn(testJob);
        
        // Act
        asyncJobService.markCompleted(testJob, resultData);
        
        // Assert
        ArgumentCaptor<AsyncJob> jobCaptor = ArgumentCaptor.forClass(AsyncJob.class);
        verify(jobRepository).save(jobCaptor.capture());
        
        AsyncJob captured = jobCaptor.getValue();
        assertThat(captured.getStatus()).isEqualTo(JobStatus.COMPLETED);
        assertThat(captured.getProgress()).isEqualTo(100);
        assertThat(captured.getResultData()).containsIgnoringCase("filePath");
        assertThat(captured.getErrorMessage()).isNull();
        assertThat(captured.getCompletedAt()).isNotNull();
    }
    
    @Test
    void testMarkFailed_SetsErrorMessageAndFailedStatus() {
        // Arrange
        when(jobRepository.save(any(AsyncJob.class))).thenReturn(testJob);
        
        // Act
        asyncJobService.markFailed(testJob, "Timeout occurred");
        
        // Assert
        ArgumentCaptor<AsyncJob> jobCaptor = ArgumentCaptor.forClass(AsyncJob.class);
        verify(jobRepository).save(jobCaptor.capture());
        
        AsyncJob captured = jobCaptor.getValue();
        assertThat(captured.getStatus()).isEqualTo(JobStatus.FAILED);
        assertThat(captured.getErrorMessage()).isEqualTo("Timeout occurred");
        assertThat(captured.getCompletedAt()).isNotNull();
    }
}
