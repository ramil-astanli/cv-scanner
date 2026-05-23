package com.cvscanner.cv_scanner.service;

import com.cvscanner.cv_scanner.entity.Candidate;
import com.cvscanner.cv_scanner.enums.ProcessingStatus;
import com.cvscanner.cv_scanner.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final CandidateRepository candidateRepository;
    private static final int PAGE_SIZE = 500;
    private static final int EXCEL_STREAMING_ROWS = 100;
    private static final String[] HEADERS = {"ID", "Full Name", "Email", "Phone", "Skills", "Experience (yrs)", "Location", "Job Type"};

    @Transactional(readOnly = true)
    public byte[] exportCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append(buildCsvRow(HEADERS)).append("\n");

        int page = 0;
        Page<Candidate> chunk;
        do {
            chunk = candidateRepository.findByProcessingStatus(ProcessingStatus.SUCCESS, PageRequest.of(page++, PAGE_SIZE));
            for (Candidate c : chunk.getContent()) {
                csv.append(buildCsvRow(
                        String.valueOf(c.getId()), c.getFullName(), c.getEmail(), c.getPhone(),
                        c.getSkills() != null ? String.join("|", c.getSkills()) : "",
                        c.getYearsOfExperience() != null ? String.valueOf(c.getYearsOfExperience()) : "",
                        c.getPreferredLocation(), String.valueOf(c.getJobType())
                )).append("\n");
            }
        } while (chunk.hasNext());

        return addUtf8Bom(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Transactional(readOnly = true)
    public byte[] exportExcel() throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(EXCEL_STREAMING_ROWS);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Candidates");
            writeHeader(sheet, workbook);

            int rowIndex = 1;
            int page = 0;
            Page<Candidate> chunk;
            do {
                chunk = candidateRepository.findByProcessingStatus(ProcessingStatus.SUCCESS, PageRequest.of(page++, PAGE_SIZE));
                for (Candidate c : chunk.getContent()) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(c.getId());
                    row.createCell(1).setCellValue(nullSafe(c.getFullName()));
                    row.createCell(2).setCellValue(nullSafe(c.getEmail()));
                    row.createCell(3).setCellValue(nullSafe(c.getPhone()));
                    row.createCell(4).setCellValue(c.getSkills() != null ? String.join(", ", c.getSkills()) : "");
                    row.createCell(5).setCellValue(c.getYearsOfExperience() != null ? c.getYearsOfExperience() : 0);
                    row.createCell(6).setCellValue(nullSafe(c.getPreferredLocation()));
                    row.createCell(7).setCellValue(c.getJobType() != null ? c.getJobType().toString() : "");
                }
            } while (chunk.hasNext());

            workbook.write(out);
            workbook.dispose();
            return out.toByteArray();
        }
    }

    // --- Köməkçi Metodlar ---
    private byte[] addUtf8Bom(byte[] content) {
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] result = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(content, 0, result, bom.length, content.length);
        return result;
    }

    private void writeHeader(Sheet sheet, Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private String buildCsvRow(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(",");
            String val = fields[i] == null ? "" : fields[i];
            sb.append("\"").append(val.replace("\"", "\"\"")).append("\"");
        }
        return sb.toString();
    }

    private String nullSafe(String value) { return value != null ? value : ""; }
}