package com.cvscanner.cv_scanner.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class JobStatusResponse {
    private String jobName;
    private String status;
    private Instant startTime;
    private Instant endTime;
    private long totalFiles;
    private long successCount;
    private long failedCount;
    private long skippedCount;
    private String duration;
}