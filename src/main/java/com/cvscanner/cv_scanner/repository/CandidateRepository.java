package com.cvscanner.cv_scanner.repository;

import com.cvscanner.cv_scanner.entity.Candidate;
import com.cvscanner.cv_scanner.enums.JobType;
import com.cvscanner.cv_scanner.enums.ProcessingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRepository
        extends JpaRepository<Candidate, Long>,
        JpaSpecificationExecutor<Candidate> {

    Page<Candidate> findByProcessingStatus(ProcessingStatus status, Pageable pageable);

    List<Candidate> findByJobType(JobType jobType);

    List<Candidate> findByPreferredLocationContainingIgnoreCase(String location);

    List<Candidate> findByYearsOfExperienceGreaterThanEqual(Integer years);

    @Query("""
        SELECT DISTINCT c FROM Candidate c
        JOIN c.skills s
        WHERE LOWER(s) LIKE LOWER(CONCAT('%', :skill, '%'))
    """)
    List<Candidate> findBySkill(@Param("skill") String skill);

    long countByProcessingStatus(ProcessingStatus status);

    boolean existsByCvFileName(String cvFileName);
}