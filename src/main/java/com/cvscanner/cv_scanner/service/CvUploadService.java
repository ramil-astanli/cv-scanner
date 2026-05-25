package com.cvscanner.cv_scanner.service;

import com.cvscanner.cv_scanner.dto.UploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class CvUploadService {

    private final FileStorageService fileStorageService;
    private final FileValidationService fileValidationService;

    @Qualifier("asyncJobLauncher")
    private final JobLauncher jobLauncher;
    private final Job cvProcessingJob;

    public CvUploadService(FileStorageService fileStorageService,
                           FileValidationService fileValidationService,
                           @Qualifier("asyncJobLauncher") JobLauncher jobLauncher,
                           Job cvProcessingJob) {
        this.fileStorageService = fileStorageService;
        this.fileValidationService = fileValidationService;
        this.jobLauncher = jobLauncher;
        this.cvProcessingJob = cvProcessingJob;
    }

    public UploadResponse uploadAndProcess(MultipartFile file) {
        fileValidationService.validateZipFile(file);

        Path tempDir = fileStorageService.unzipToTemp(file);

        try {
            List<File> cvFiles = fileStorageService.listCvFiles(tempDir);

            if (cvFiles.isEmpty()) {
                fileStorageService.cleanupDirectory(tempDir);
                throw new IllegalArgumentException("ZIP içində PDF/DOCX fayl tapılmadı");
            }

            log.info("{} CV faylı tapıldı → batch job başladılır", cvFiles.size());

            JobParameters params = new JobParametersBuilder()
                    .addString("tempDir", tempDir.toAbsolutePath().toString())
                    .addLong("startTime", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(cvProcessingJob, params);

            log.info("Batch job trigger edildi. ID: {}, Status: {}",
                    execution.getId(), execution.getStatus());

            return UploadResponse.builder()
                    .success(true)
                    .message("CV emalı növbəyə alındı və başladıldı")
                    .totalFiles(cvFiles.size())
                    .tempDirectory(tempDir.toString())
                    .jobStatus(execution.getStatus().toString())
                    .timestamp(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("Batch job başlatma xətası - qovluq təmizlənir: {}", tempDir, e);
            fileStorageService.cleanupDirectory(tempDir);

            if (e instanceof IllegalArgumentException) throw (IllegalArgumentException) e;
            throw new RuntimeException("Sistem xətası: Job başladılarkən problem yarandı", e);
        }
    }
}