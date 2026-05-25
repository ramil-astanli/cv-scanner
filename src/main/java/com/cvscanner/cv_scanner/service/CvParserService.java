package com.cvscanner.cv_scanner.service;

import com.cvscanner.cv_scanner.enums.JobType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class CvParserService {

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "([A-ZÀ-Ö][a-zà-ö]+(?:\\s[A-ZÀ-Ö][a-zà-ö]+){1,2})"
    );

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "(?:\\+?\\d{1,3}[\\s\\-]?)?(?:\\(?\\d{2,4}\\)?[\\s\\-]?)?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}"
    );

    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile(
            "(\\d+)\\s*(?:years?|yıl|il|year|yrs?)\\s*(?:of\\s*)?(?:experience|work|təcrübə)?",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );

    private static final List<String> KNOWN_SKILLS = List.of(
        "Java", "Spring Boot", "Spring Batch", "Spring MVC",
        "Python", "Django", "FastAPI",
        "JavaScript", "TypeScript", "React", "Angular", "Vue",
        "Node.js", "Express",
        "PostgreSQL", "MySQL", "MongoDB", "Redis",
        "Docker", "Kubernetes", "Jenkins", "CI/CD",
        "AWS", "Azure", "GCP",
        "Git", "Maven", "Gradle",
        "REST", "GraphQL", "Microservices",
        "Hibernate", "JPA", "Kafka", "RabbitMQ",
        "Linux", "Bash", "Terraform"
    );

    public String parseName(String text) {
        if (text == null || text.isBlank()) return "Unknown Candidate";

        String leadText = text.length() > 500 ? text.substring(0, 500) : text;
        Matcher matcher = NAME_PATTERN.matcher(leadText);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "Unknown";
    }

    public String parseEmail(String text) {
        if (text == null || text.isBlank()) return null;

        Matcher matcher = EMAIL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim().toLowerCase();
        }
        return null;
    }

    public String parsePhone(String text) {
        if (text == null || text.isBlank()) return null;

        Matcher matcher = PHONE_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return null;
    }

    public Integer parseYearsOfExperience(String text) {
        if (text == null || text.isBlank()) return 0;

        Matcher matcher = EXPERIENCE_PATTERN.matcher(text);
        int maxYears = 0;

        while (matcher.find()) {
            try {
                int years = Integer.parseInt(matcher.group(1));
                if (years > maxYears && years <= 40) {
                    maxYears = years;
                }
            } catch (NumberFormatException e) {
                log.warn("İl formatı oxuna bilmədi: {}", matcher.group(1));
            }
        }
        return maxYears;
    }

    public List<String> parseSkills(String text) {
        if (text == null || text.isBlank()) return new ArrayList<>();

        List<String> found = new ArrayList<>();
        String lowerText = text.toLowerCase();

        for (String skill : KNOWN_SKILLS) {
            if (lowerText.contains(skill.toLowerCase())) {
                found.add(skill);
            }
        }

        log.debug("Tapılan skills: {}", found);
        return found;
    }

    public String parseLocation(String text) {
        if (text == null || text.isBlank()) return null;

        List<String> cities = List.of(
            "Baku", "Bakı", "Istanbul", "London", "Berlin",
            "Amsterdam", "Warsaw", "Prague", "Budapest",
            "Dubai", "Remote", "Uzaqdan"
        );

        String lowerText = text.toLowerCase();
        for (String city : cities) {
            if (lowerText.contains(city.toLowerCase())) {
                return city;
            }
        }
        return null;
    }

    public JobType parseJobType(String text) {
        if (text == null || text.isBlank()) return JobType.UNKNOWN;

        String lower = text.toLowerCase();

        if (lower.contains("remote") || lower.contains("uzaqdan")) {
            return JobType.REMOTE;
        }
        if (lower.contains("hybrid") || lower.contains("hibrid")) {
            return JobType.HYBRID;
        }
        if (lower.contains("onsite") || lower.contains("on-site") ||
            lower.contains("office") || lower.contains("ofis")) {
            return JobType.ONSITE;
        }
        return JobType.UNKNOWN;
    }
}