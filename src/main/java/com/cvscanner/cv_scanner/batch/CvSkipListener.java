package com.cvscanner.cv_scanner.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class CvSkipListener implements SkipListener<File, Object> {

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("READ zamanı skip: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(File file, Throwable t) {
        log.error("PROCESS zamanı skip: {} → {}", file.getName(), t.getMessage());
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.error("WRITE zamanı skip: {}", t.getMessage());
    }
}