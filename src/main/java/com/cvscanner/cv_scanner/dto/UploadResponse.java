package com.cvscanner.cv_scanner.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UploadResponse {
    private boolean success;
    private String message;
    private int totalFiles;        // ZIP içindəki fayl sayı
    private String tempDirectory;  // faylların saxlandığı qovluq
    private String jobStatus;      // batch job statusu
    private Instant timestamp;
}
