package com.cvscanner.cv_scanner.batch;

import com.cvscanner.cv_scanner.service.FileStorageService;
import com.cvscanner.cv_scanner.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobCompletionListener implements JobExecutionListener {

    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Batch job started: {}",
            jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String status = jobExecution.getStatus().toString();

        long successCount = jobExecution.getStepExecutions()
            .stream()
            .mapToLong(StepExecution::getWriteCount)
            .sum();

        long failedCount = jobExecution.getStepExecutions()
            .stream()
            .mapToLong(s -> s.getProcessSkipCount()
                           + s.getWriteSkipCount())
            .sum();

        long skippedCount = jobExecution.getStepExecutions()
            .stream()
            .mapToLong(StepExecution::getReadSkipCount)
            .sum();

        String duration = calculateDuration(jobExecution);

        log.info("Job completed [{}] → {} successful, {} failed",
            status, successCount, failedCount);

        String tempDir = jobExecution
            .getJobParameters()
            .getString("tempDir");

        if (tempDir != null) {
            fileStorageService.cleanupDirectory(Paths.get(tempDir));
        }

        notificationService.sendJobCompletionEmail(
            status, successCount,
            failedCount, skippedCount, duration
        );
    }

    private String calculateDuration(JobExecution jobExecution) {
        if (jobExecution.getStartTime() == null ||
            jobExecution.getEndTime() == null) {
            return "N/A";
        }
        Duration d = Duration.between(
            jobExecution.getStartTime(),
            jobExecution.getEndTime()
        );
        return d.toSeconds() + " second";
    }
}