package com.cvscanner.cv_scanner.service;

import com.cvscanner.cv_scanner.dto.JobStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobExplorer jobExplorer;
    // Repository-ni hələ də saxlaya bilərsən, əgər ümumi statistika lazımdırsa

    public JobStatusResponse getLastJobStatus() {
        JobExecution lastExecution = jobExplorer.findJobInstancesByJobName("cvProcessingJob", 0, 1)
                .stream()
                .flatMap(i -> jobExplorer.getJobExecutions(i).stream())
                .max(Comparator.comparing(JobExecution::getStartTime)) // Ən sonuncunu tapırıq
                .orElse(null);

        if (lastExecution == null) {
            return JobStatusResponse.builder().jobName("cvProcessingJob").status("NEVER_RUN").build();
        }

        // Real vaxtda o işdə neçə fayl keçib:
        long successCount = lastExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getWriteCount).sum();
        long failedCount = lastExecution.getStepExecutions().stream()
                .mapToLong(s -> s.getProcessSkipCount() + s.getWriteSkipCount()).sum();

        long skippedCount = lastExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getReadSkipCount).sum();

        return JobStatusResponse.builder()
                .jobName(lastExecution.getJobInstance().getJobName())
                .status(lastExecution.getStatus().toString())
                .startTime(toInstant(lastExecution.getStartTime()))
                .endTime(toInstant(lastExecution.getEndTime()))
                .successCount(successCount)
                .failedCount(failedCount)
                .skippedCount(skippedCount)
                .totalFiles(successCount + failedCount)
                .build();
    }

    public List<JobStatusResponse> getJobHistory() {
        return jobExplorer.findJobInstancesByJobName("cvProcessingJob", 0, 10)
                .stream()
                .flatMap(i -> jobExplorer.getJobExecutions(i).stream())
                .sorted(Comparator.comparing(JobExecution::getStartTime).reversed()) // Tarixə görə sırala
                .map(exec -> JobStatusResponse.builder()
                        .jobName(exec.getJobInstance().getJobName())
                        .status(exec.getStatus().toString())
                        .startTime(toInstant(exec.getStartTime()))
                        .endTime(toInstant(exec.getEndTime()))
                        .build())
                .toList();
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}