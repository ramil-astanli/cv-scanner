package com.cvscanner.cv_scanner.exception;

import com.cvscanner.cv_scanner.dto.UploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<UploadResponse> handleValidation(IllegalArgumentException e) {
        log.warn("Validasiya xətası: {}", e.getMessage());
        return ResponseEntity.badRequest().body(
            UploadResponse.builder()
                .success(false)
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build()
        );
    }

    @ExceptionHandler(JobExecutionAlreadyRunningException.class)
    public ResponseEntity<UploadResponse> handleJobRunning(JobExecutionAlreadyRunningException e) {
        log.warn("Job artıq işləyir");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            UploadResponse.builder()
                .success(false)
                .message("Batch job artıq işləyir, gözləyin")
                .timestamp(Instant.now())
                .build()
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<UploadResponse> handleMaxSize(MaxUploadSizeExceededException e) {
        log.warn("Fayl ölçüsü həddini keçdi");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
            UploadResponse.builder()
                .success(false)
                .message("Fayl ölçüsü 500MB həddini keçir")
                .timestamp(Instant.now())
                .build()
        );
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<UploadResponse> handleStorage(FileStorageException e) {
        log.error("Fayl saxlama xətası: {}", e.getMessage());
        return ResponseEntity.internalServerError().body(
            UploadResponse.builder()
                .success(false)
                .message("Fayl əməliyyatı zamanı xəta baş verdi")
                .timestamp(Instant.now())
                .build()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<UploadResponse> handleRuntime(RuntimeException e) {
        log.error("Gözlənilməz xəta", e);
        return ResponseEntity.internalServerError().body(
            UploadResponse.builder()
                .success(false)
                .message("Server xətası: " + e.getMessage())
                .timestamp(Instant.now())
                .build()
        );
    }
}