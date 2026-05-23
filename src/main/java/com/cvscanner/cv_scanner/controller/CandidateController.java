package com.cvscanner.cv_scanner.controller;

import com.cvscanner.cv_scanner.dto.CandidateResponse;
import com.cvscanner.cv_scanner.enums.JobType;
import com.cvscanner.cv_scanner.enums.ProcessingStatus;
import com.cvscanner.cv_scanner.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidates", description = "Kandidat axtarışı və filtrasiyası")
public class CandidateController {

    private final CandidateService candidateService;

    @GetMapping
    @Operation(summary = "Kandidatları filtrə et və səhifələ")
    public ResponseEntity<Page<CandidateResponse>> getCandidates(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) ProcessingStatus status,

            // Default olaraq ən yeni əlavə edilənlər yuxarıda, hər səhifədə 10 nəfər
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(
                candidateService.getCandidates(skill, minExperience, location, jobType, status, pageable)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID ilə kandidat tap")
    public ResponseEntity<CandidateResponse> getById(@PathVariable Long id) {
        return candidateService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/count")
    @Operation(summary = "Sistemdəki ümumi kandidat sayı")
    public ResponseEntity<Long> getCount() {
        return ResponseEntity.ok(candidateService.getCount());
    }
}