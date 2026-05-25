package com.cvscanner.cv_scanner.service;

import com.cvscanner.cv_scanner.dto.CandidateResponse;
import com.cvscanner.cv_scanner.entity.Candidate;
import com.cvscanner.cv_scanner.enums.JobType;
import com.cvscanner.cv_scanner.enums.ProcessingStatus;
import com.cvscanner.cv_scanner.repository.CandidateRepository;
import com.cvscanner.cv_scanner.specification.CandidateSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;

    @Transactional(readOnly = true)
    public Page<CandidateResponse> getCandidates(
            String skill,
            Integer minExperience,
            String location,
            JobType jobType,
            ProcessingStatus status,
            Pageable pageable) {

        Specification<Candidate> spec = CandidateSpec.filter(
                skill, minExperience, location, jobType, status
        );

        return candidateRepository.findAll(spec, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<CandidateResponse> getById(Long id) {
        return candidateRepository.findById(id)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long getCount() {
        return candidateRepository.count();
    }

    private CandidateResponse toResponse(Candidate c) {
        return CandidateResponse.builder()
                .id(c.getId())
                .fullName(c.getFullName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .skills(c.getSkills() != null
                        ? new ArrayList<>(c.getSkills())
                        : List.of())
                .yearsOfExperience(c.getYearsOfExperience())
                .preferredLocation(c.getPreferredLocation())
                .jobType(c.getJobType())
                .processingStatus(c.getProcessingStatus())
                .cvFileName(c.getCvFileName())
                .createdAt(c.getCreatedAt())
                .build();
    }
}