package com.cvscanner.cv_scanner.exception;

import com.cvscanner.cv_scanner.dto.UploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Validasiya xətaları — 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<UploadResponse> handleValidation(IllegalArgumentException e) {
        log.warn("Validasiya xətası: {}", e.getMessage());
        return ResponseEntity.badRequest().body(
            UploadResponse.builder()
                .success(false)
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    // Batch job artıq işləyir — 409 Conflict
    @ExceptionHandler(JobExecutionAlreadyRunningException.class)
    public ResponseEntity<UploadResponse> handleJobRunning(JobExecutionAlreadyRunningException e) {
        log.warn("Job artıq işləyir");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            UploadResponse.builder()
                .success(false)
                .message("Batch job artıq işləyir, gözləyin")
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    // Fayl ölçüsü həddini keçdi — 413
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<UploadResponse> handleMaxSize(MaxUploadSizeExceededException e) {
        log.warn("Fayl ölçüsü həddini keçdi");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
            UploadResponse.builder()
                .success(false)
                .message("Fayl ölçüsü 500MB həddini keçir")
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    // FileStorage xətaları — 500
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<UploadResponse> handleStorage(FileStorageException e) {
        log.error("Fayl saxlama xətası: {}", e.getMessage());
        return ResponseEntity.internalServerError().body(
            UploadResponse.builder()
                .success(false)
                .message("Fayl əməliyyatı zamanı xəta baş verdi")
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    // Gözlənilməz xətalar — 500
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<UploadResponse> handleRuntime(RuntimeException e) {
        log.error("Gözlənilməz xəta", e);
        return ResponseEntity.internalServerError().body(
            UploadResponse.builder()
                .success(false)
                .message("Server xətası: " + e.getMessage())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
}