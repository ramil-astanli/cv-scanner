package com.cvscanner.cv_scanner.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.notification.hr-email}")
    private String hrEmail;

    @Value("${app.notification.enabled:true}")
    private boolean notificationEnabled;

    @Async
    public void sendJobCompletionEmail(
            String jobStatus,
            long successCount,
            long failedCount,
            long skippedCount,
            String duration) {

        if (!notificationEnabled) {
            log.info("Email bildirişi deaktivdir");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message, true, "UTF-8"
            );

            helper.setTo(hrEmail);
            helper.setSubject(buildSubject(jobStatus));
            helper.setText(buildHtmlBody(
                jobStatus, successCount,
                failedCount, skippedCount, duration
            ), true);

            mailSender.send(message);
            log.info("Email göndərildi: {}", hrEmail);

        } catch (MessagingException e) {
            log.error("Email göndərilə bilmədi", e);
        }
    }

    private String buildSubject(String status) {
        return status.equals("COMPLETED")
            ? "CV Scanner — Batch Job Ugurla Tamamlandi"
            : "CV Scanner — Batch Job Xeta ile Bitdi: " + status;
    }

    private String buildHtmlBody(
            String status,
            long success,
            long failed,
            long skipped,
            String duration) {

        String color = status.equals("COMPLETED")
            ? "#28a745" : "#dc3545";

        return """
            <html>
            <body style="font-family:Arial,sans-serif;padding:20px">
              <h2>CV Scanner — Batch Job Hesabati</h2>
              <hr/>
              <p>
                <strong>Status:</strong>
                <span style="color:%s;font-weight:bold">%s</span>
              </p>
              <table style="border-collapse:collapse;width:400px">
                <tr style="background:#f8f9fa">
                  <td style="padding:8px;border:1px solid #ddd">
                    Ugurlu CV
                  </td>
                  <td style="padding:8px;border:1px solid #ddd;
                             color:#28a745;font-weight:bold">
                    %d
                  </td>
                </tr>
                <tr>
                  <td style="padding:8px;border:1px solid #ddd">
                    Xetali CV
                  </td>
                  <td style="padding:8px;border:1px solid #ddd;
                             color:#dc3545;font-weight:bold">
                    %d
                  </td>
                </tr>
                <tr style="background:#f8f9fa">
                  <td style="padding:8px;border:1px solid #ddd">
                    Kecilen CV
                  </td>
                  <td style="padding:8px;border:1px solid #ddd;
                             color:#ffc107;font-weight:bold">
                    %d
                  </td>
                </tr>
                <tr>
                  <td style="padding:8px;border:1px solid #ddd">
                    Muddet
                  </td>
                  <td style="padding:8px;border:1px solid #ddd">
                    %s
                  </td>
                </tr>
              </table>
              <hr/>
              <p style="color:#666;font-size:12px">
                Bu email CVScanner sistemi terefinden avtomatik
                gonderilmisdir.
              </p>
            </body>
            </html>
            """.formatted(color, status,
                success, failed, skipped, duration);
    }
}