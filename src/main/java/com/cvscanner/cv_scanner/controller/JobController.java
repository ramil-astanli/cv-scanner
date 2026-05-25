package com.cvscanner.cv_scanner.controller;

import com.cvscanner.cv_scanner.dto.JobStatusResponse;
import com.cvscanner.cv_scanner.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Monitoring", description = "Batch job status and history")
public class JobController {

    private final JobService jobService;

    @GetMapping("/status")
    @Operation(summary = "Last batch job status")
    public ResponseEntity<JobStatusResponse> getStatus() {
        return ResponseEntity.ok(jobService.getLastJobStatus());
    }

    @GetMapping("/history")
    @Operation(summary = "Batch job history")
    public ResponseEntity<List<JobStatusResponse>> getHistory() {
        return ResponseEntity.ok(jobService.getJobHistory());
    }
}

