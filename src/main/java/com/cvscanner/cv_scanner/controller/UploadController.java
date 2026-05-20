package com.cvscanner.cv_scanner.controller;

import com.cvscanner.cv_scanner.dto.UploadResponse;
import com.cvscanner.cv_scanner.service.CvUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "CV Upload", description = "CV fayllarını yükləmə və batch job başlatma")
public class UploadController {

    private final CvUploadService cvUploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "ZIP faylı yüklə və batch job başlat")
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("file") MultipartFile file) {

        UploadResponse response = cvUploadService.uploadAndProcess(file);
        return ResponseEntity.ok(response);
    }
}