package com.cvscanner.cv_scanner.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileValidationService {

    private final Tika tika = new Tika();

    // Sadəcə uzantıya baxmır — Tika ilə real MIME type yoxlayır
    public void validateZipFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fayl boşdur");
        }

        try {
            String mimeType = tika.detect(file.getInputStream());
            log.debug("Yüklənən faylın MIME tipi: {}", mimeType);

            if (!mimeType.equals("application/zip") &&
                !mimeType.equals("application/x-zip-compressed")) {
                throw new IllegalArgumentException(
                    "Yalnız ZIP formatı qəbul edilir. Aşkarlanan tip: " + mimeType
                );
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Fayl oxuna bilmədi", e);
        }
    }
}