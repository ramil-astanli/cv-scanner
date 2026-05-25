package com.cvscanner.cv_scanner.specification;

import com.cvscanner.cv_scanner.entity.Candidate;
import com.cvscanner.cv_scanner.enums.JobType;
import com.cvscanner.cv_scanner.enums.ProcessingStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CandidateSpec {

    public static Specification<Candidate> filter(
            String skill,
            Integer minExperience,
            String location,
            JobType jobType,
            ProcessingStatus status) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (skill != null && !skill.isBlank()) {
                Join<Candidate, String> skillJoin = root.join("skills", JoinType.INNER);
                predicates.add(
                        cb.like(cb.lower(skillJoin.as(String.class)),
                                "%" + skill.toLowerCase() + "%")
                );
                query.distinct(true);
            }

            if (minExperience != null && minExperience > 0) {
                predicates.add(
                    cb.greaterThanOrEqualTo(
                        root.get("yearsOfExperience"), minExperience)
                );
            }

            if (location != null && !location.isBlank()) {
                predicates.add(
                    cb.like(cb.lower(root.get("preferredLocation")),
                        "%" + location.toLowerCase() + "%")
                );
            }

            if (jobType != null) {
                predicates.add(
                    cb.equal(root.get("jobType"), jobType)
                );
            }

            if (status != null) {
                predicates.add(
                    cb.equal(root.get("processingStatus"), status)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}