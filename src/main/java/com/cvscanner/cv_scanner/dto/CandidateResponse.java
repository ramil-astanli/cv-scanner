package com.cvscanner.cv_scanner.dto;

import com.cvscanner.cv_scanner.enums.JobType;
import com.cvscanner.cv_scanner.enums.ProcessingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class CandidateResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private List<String> skills;
    private Integer yearsOfExperience;
    private String preferredLocation;
    private JobType jobType;
    private ProcessingStatus processingStatus;
    private String cvFileName;
    private Instant createdAt;
}