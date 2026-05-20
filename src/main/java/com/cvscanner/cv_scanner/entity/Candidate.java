package com.cvscanner.cv_scanner.entity;

import com.cvscanner.cv_scanner.enums.JobType;
import com.cvscanner.cv_scanner.enums.ProcessingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cv_file_name")
    private String cvFileName;

    @Column(name = "cv_file_path")
    private String cvFilePath;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    // Skills siyahısı — DB-də vergüllə ayrılmış mətn kimi saxlanır
    // Məs: "Java,Spring Boot,PostgreSQL,Docker"
    @ElementCollection
    @CollectionTable(
            name = "candidate_skills",
            joinColumns = @JoinColumn(name = "candidate_id")
    )
    @Column(name = "skill")
    private List<String> skills;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "preferred_location")
    private String preferredLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type")
    private JobType jobType;

    // Batch processing nəticəsi
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    private ProcessingStatus processingStatus;

    // Xəta varsa səbəbi
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}