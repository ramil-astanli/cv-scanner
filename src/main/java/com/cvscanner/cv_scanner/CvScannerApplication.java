package com.cvscanner.cv_scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
public class CvScannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CvScannerApplication.class, args);
	}

}
