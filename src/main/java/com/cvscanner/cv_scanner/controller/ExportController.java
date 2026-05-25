package com.cvscanner.cv_scanner.controller;

import com.cvscanner.cv_scanner.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/candidates/export")
@RequiredArgsConstructor
@Tag(name = "Export", description = "Download candidate data as CSV or Excel")
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/csv")
    @Operation(summary = "Download as CSV")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=candidates.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(exportService.exportCsv());
    }

    @GetMapping("/xlsx")
    @Operation(summary = "Download as Excel")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=candidates.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument" +
                                ".spreadsheetml.sheet"))
                .body(exportService.exportExcel());
    }
}