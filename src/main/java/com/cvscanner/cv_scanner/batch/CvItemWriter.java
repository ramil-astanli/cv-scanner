package com.cvscanner.cv_scanner.batch;

import com.cvscanner.cv_scanner.entity.Candidate;
import com.cvscanner.cv_scanner.enums.ProcessingStatus;
import com.cvscanner.cv_scanner.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CvItemWriter implements ItemWriter<Candidate> {

    private final CandidateRepository candidateRepository;

    @Override
    public void write(Chunk<? extends Candidate> chunk) throws Exception {
        long successCount = chunk.getItems().stream()
            .filter(c -> c.getProcessingStatus() == ProcessingStatus.SUCCESS)
            .count();

        long failedCount = chunk.size() - successCount;

        candidateRepository.saveAll(chunk.getItems());

        log.info("Chunk written → {} successful, {} failed (total: {})",
            successCount, failedCount, chunk.size());
    }
}