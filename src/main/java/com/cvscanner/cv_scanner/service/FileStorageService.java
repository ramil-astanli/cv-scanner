package com.cvscanner.cv_scanner.service;

import com.cvscanner.cv_scanner.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.upload.temp-dir}")
    private String tempDir;

    public Path unzipToTemp(MultipartFile zipFile) {
        String sessionId = UUID.randomUUID().toString();
        Path sessionDir = Paths.get(tempDir, sessionId);

        try {
            Files.createDirectories(sessionDir);
            log.info("Temp qovluq yaradıldı: {}", sessionDir);

            unzip(zipFile, sessionDir);

            log.info("ZIP açıldı: {} → {}", zipFile.getOriginalFilename(), sessionDir);
            return sessionDir;

        } catch (IOException e) {
            throw new FileStorageException(
                    "ZIP faylı açıla bilmədi: " + zipFile.getOriginalFilename(), e
            );
        }
    }

    private void unzip(MultipartFile zipFile, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;

            Path absoluteTargetDir = targetDir.toAbsolutePath().normalize();

            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = targetDir.resolve(entry.getName()).toAbsolutePath().normalize();

                if (!entryPath.startsWith(absoluteTargetDir)) {
                    log.warn("Zip Slip cəhdi aşkarlandı: {}", entry.getName());
                    zis.closeEntry();
                    continue;
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    if (isSupportedFile(entry.getName())) {
                        Files.createDirectories(entryPath.getParent());
                        Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                        log.debug("Fayl çıxarıldı: {}", entry.getName());
                    } else {
                        log.debug("Dəstəklənməyən fayl atlandı: {}", entry.getName());
                    }
                }
                zis.closeEntry();
            }
        }
    }

    public List<File> listCvFiles(Path directory) {
        List<File> files = new ArrayList<>();
        try {
            Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .filter(p -> isSupportedFile(p.toString()))
                    .forEach(p -> files.add(p.toFile()));
        } catch (IOException e) {
            throw new FileStorageException(
                    "Qovluq oxuna bilmədi: " + directory, e
            );
        }
        log.info("Tapılan CV faylları: {} ədəd", files.size());
        return files;
    }

    public void cleanupDirectory(Path directory) {
        try {
            Files.walk(directory)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("Fayl silinə bilmədi: {}", path);
                        }
                    });
            log.info("Temp qovluq silindi: {}", directory);
        } catch (IOException e) {
            log.error("Cleanup zamanı xəta: {}", directory, e);
        }
    }

    private boolean isSupportedFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".pdf") || lower.endsWith(".docx") || lower.endsWith(".zip");
    }
}