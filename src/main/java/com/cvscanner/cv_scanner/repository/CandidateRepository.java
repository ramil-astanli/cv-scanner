package com.cvscanner.cv_scanner.repository;

import com.cvscanner.cv_scanner.entity.Candidate;
import com.cvscanner.cv_scanner.enums.JobType;
import com.cvscanner.cv_scanner.enums.ProcessingStatus;
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

    // Status üzrə filtrlə
    List<Candidate> findByProcessingStatus(ProcessingStatus status);

    // İş tipi üzrə filtrlə
    List<Candidate> findByJobType(JobType jobType);

    // Lokasiya üzrə filtrlə (case-insensitive)
    List<Candidate> findByPreferredLocationContainingIgnoreCase(String location);

    // Minimum təcrübə üzrə filtrlə
    List<Candidate> findByYearsOfExperienceGreaterThanEqual(Integer years);

    // Müəyyən skill-ə sahib kandidatlar
    @Query("""
        SELECT DISTINCT c FROM Candidate c
        JOIN c.skills s
        WHERE LOWER(s) LIKE LOWER(CONCAT('%', :skill, '%'))
    """)
    List<Candidate> findBySkill(@Param("skill") String skill);

    // Uğurla işlənmiş sayı
    long countByProcessingStatus(ProcessingStatus status);

    // Faylın artıq işlənib-işlənmədiyini yoxla
    boolean existsByCvFileName(String cvFileName);
}