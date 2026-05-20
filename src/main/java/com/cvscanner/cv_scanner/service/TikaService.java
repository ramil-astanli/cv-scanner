package com.cvscanner.cv_scanner.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Service
public class TikaService {

    private final AutoDetectParser parser = new AutoDetectParser();

    // Fayldan xam mətn çıxarır
    // -1 → mətn ölçüsünə limit qoymuruq (böyük CV-lər üçün)
    public String extractText(File file) throws TikaException, IOException, SAXException {
        // Limit qoymaq (məsələn 10MB) RAM-ı qoruyur
        BodyContentHandler handler = new BodyContentHandler(10 * 1024 * 1024);
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        try (FileInputStream stream = new FileInputStream(file)) {
            parser.parse(stream, handler, metadata, context);
            String text = handler.toString();

            // Əgər body boşdursa, metadata-dan bəzi hissələri (məsələn Title) əlavə et
            if (text.trim().isEmpty()) {
                // TikaCoreProperties istifadə edərək başlığı götürürük
                text = metadata.get(TikaCoreProperties.TITLE) != null
                        ? metadata.get(TikaCoreProperties.TITLE)
                        : "";
            }

            log.debug("Tika mətni çıxardı: {} → {} simvol", file.getName(), text.length());
            return text;
        }
    }

    // Faylın oxunub-oxunmadığını test edir — corrupted fayllar üçün
    public boolean isReadable(File file) {
        try {
            String text = extractText(file);
            return text != null && !text.isBlank();
        } catch (Exception e) {
            log.warn("Fayl oxuna bilmir: {}", file.getName());
            return false;
        }
    }
}