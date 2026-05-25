package com.cvscanner.cv_scanner.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class CvItemReader implements ItemReader<File> {

    private Iterator<File> fileIterator;
    private int totalFiles;
    private int readCount = 0;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        String tempDir = stepExecution
            .getJobParameters()
            .getString("tempDir");

        log.info("CvItemReader started. Folder: {}", tempDir);

        Path dirPath = Paths.get(tempDir);

        try (Stream<Path> paths = Files.walk(dirPath)) {
            List<File> files = paths
                .filter(Files::isRegularFile)
                .filter(p -> {
                    String name = p.toString().toLowerCase();
                    return name.endsWith(".pdf") || name.endsWith(".docx");
                })
                .map(Path::toFile)
                .toList();

            this.totalFiles = files.size();
            this.fileIterator = files.iterator();
            log.info("CV files to be read: {} pieces", totalFiles);

        } catch (Exception e) {
            log.error("The folder could not be read: {}", tempDir, e);
            this.fileIterator = List.<File>of().iterator();
        }
    }

    @Override
    public File read()
            throws UnexpectedInputException, ParseException,
                   NonTransientResourceException {

        if (fileIterator != null && fileIterator.hasNext()) {
            File file = fileIterator.next();
            readCount++;
            log.debug("Reading [{}/{}]: {}", readCount, totalFiles, file.getName());
            return file;
        }

        log.info("All files read: {} number", readCount);
        return null;
    }
}