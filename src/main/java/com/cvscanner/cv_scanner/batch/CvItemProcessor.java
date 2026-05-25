package com.cvscanner.cv_scanner.batch;

import com.cvscanner.cv_scanner.entity.Candidate;
import com.cvscanner.cv_scanner.enums.ProcessingStatus;
import com.cvscanner.cv_scanner.service.CvParserService;
import com.cvscanner.cv_scanner.service.TikaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CvItemProcessor implements ItemProcessor<File, Candidate> {

    private final TikaService tikaService;
    private final CvParserService cvParserService;

    @Override
    public Candidate process(File file) throws Exception {
        log.info("Working: {}", file.getName());

        String rawText;
        try {
            rawText = tikaService.extractText(file);
        } catch (Exception e) {
            log.warn("Tika couldn't read: {} → {}", file.getName(), e.getMessage());
            return buildFailedCandidate(file, ProcessingStatus.SKIPPED, e.getMessage());
        }

        if (rawText == null || rawText.isBlank()) {
            log.warn("Empty text: {}", file.getName());
            return buildFailedCandidate(file, ProcessingStatus.SKIPPED, "Empty text");
        }

        try {
            String name     = cvParserService.parseName(rawText);
            String email    = cvParserService.parseEmail(rawText);
            String phone    = cvParserService.parsePhone(rawText);
            List<String> skills = cvParserService.parseSkills(rawText);
            Integer years   = cvParserService.parseYearsOfExperience(rawText);
            String location = cvParserService.parseLocation(rawText);

            Candidate candidate = Candidate.builder()
                .cvFileName(file.getName())
                .cvFilePath(file.getAbsolutePath())
                .fullName(name)
                .email(email)
                .phone(phone)
                .skills(skills)
                .yearsOfExperience(years)
                .preferredLocation(location)
                .jobType(cvParserService.parseJobType(rawText))
                .processingStatus(ProcessingStatus.SUCCESS)
                .build();

            log.info("Parsed: {} → {} skill, {} year experience",
                file.getName(), skills.size(), years);

            return candidate;

        } catch (Exception e) {
            log.error("Parse error: {}", file.getName(), e);
            return buildFailedCandidate(file, ProcessingStatus.FAILED, e.getMessage());
        }
    }

    private Candidate buildFailedCandidate(File file,
                                            ProcessingStatus status,
                                            String errorMessage) {
        return Candidate.builder()
            .cvFileName(file.getName())
            .cvFilePath(file.getAbsolutePath())
            .processingStatus(status)
            .errorMessage(errorMessage)
            .build();
    }
}