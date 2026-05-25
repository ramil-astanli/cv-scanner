package com.cvscanner.cv_scanner.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UploadResponse {
    private boolean success;
    private String message;
    private int totalFiles;
    private String tempDirectory;
    private String jobStatus;
    private Instant timestamp;
}
