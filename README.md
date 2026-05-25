# CVScanner — Automated Bulk CV Parsing and Extraction System

> A production-grade backend system designed for HR teams and recruitment platforms to automatically process and extract structured information from thousands of CVs (PDF/DOCX) using Spring Batch.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Bonus Features](#bonus-features)

---

## Overview

CVScanner eliminates manual CV review by automating the entire parsing pipeline. HR teams upload a single ZIP archive — the system unpacks it, extracts candidate information using Apache Tika, stores results in PostgreSQL, and sends a completion notification via email.

**Key use case:** A recruiter uploads a ZIP containing 1,000 CVs. Within minutes, all candidates are parsed, stored, and searchable by skill, experience level, location, or job preference — with no manual intervention.

---

## Features

| Feature | Description |
|---|---|
| Bulk CV Upload | Accept a ZIP file containing hundreds or thousands of CVs |
| Text Extraction | Extract raw text from PDF and DOCX using Apache Tika |
| Smart Parsing | Regex-based extraction of name, email, phone, skills, experience, location, job type |
| Batch Processing | Chunk-oriented processing with Spring Batch (Job → Step → Reader → Processor → Writer) |
| Fault Tolerance | Retry failed files up to 3 times; skip corrupted files with full logging |
| Filter API | Search candidates by skill, minimum experience, location, job type, and status |
| Pagination | Paginated and sorted results via Spring Data Pageable |
| CSV Export | Download all parsed candidates as a `.csv` file |
| Excel Export | Download all parsed candidates as a `.xlsx` file (Apache POI) |
| Job Monitoring | Track batch job status, success/failure counts, and execution history via REST API |
| Email Notification | HTML email sent to HR automatically after each batch job completes |
| Swagger UI | Interactive API documentation at `/swagger-ui.html` |

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Spring Boot | 3.5.x | Application framework |
| Spring Batch | 5.x | Bulk file processing pipeline |
| Spring Data JPA | — | ORM and repository layer |
| Apache Tika | 2.9.2 | PDF/DOCX text extraction |
| Apache POI | 5.3.0 | Excel (.xlsx) generation |
| PostgreSQL | 16 | Relational database |
| Flyway | — | Database schema versioning |
| JavaMailSender | — | Email notifications via SMTP |
| Springdoc OpenAPI | 2.8.9 | Swagger UI and API docs |
| Docker Compose | — | Local environment orchestration |
| Lombok | — | Boilerplate reduction |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     HR Client                           │
│              POST /api/upload (ZIP file)                │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                  UploadController                       │
│         Validates ZIP → Delegates to Service            │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                  CvUploadService                        │
│    Unzip to temp dir → Launch Spring Batch Job          │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Batch Pipeline                      │
│                                                         │
│   CvItemReader → CvItemProcessor → CvItemWriter         │
│       │               │                 │               │
│   Read files    Tika + Parser      Save to DB           │
│   from temp     extract data       (PostgreSQL)         │
│   directory                                             │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│             JobCompletionListener                       │
│       Cleanup temp dir → Send email to HR               │
└─────────────────────────────────────────────────────────┘
```

---

## Getting Started

### Prerequisites

- Java 21
- Maven 3.8+
- Docker Desktop

### 1. Clone the repository

```bash
git clone https://github.com/your-username/cv-scanner.git
cd cv-scanner
```

### 2. Configure environment variables

In IntelliJ: **Run → Edit Configurations → Environment Variables**

```
DB_PASSWORD=your_db_password
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
```

> **How to get a Gmail App Password:**
> Google Account → Security → 2-Step Verification → App Passwords → Create

### 3. Configure HR notification email

In `application.properties`:

```properties
app.notification.hr-email=hr@yourcompany.com
app.notification.enabled=true
```

### 4. Start Docker services

```bash
docker compose up -d
```

Verify PostgreSQL is healthy:

```bash
docker ps
# cvscanner-db should show (healthy)
```

### 5. Run the application

```bash
mvn spring-boot:run
```

### 6. Open Swagger UI

```
http://localhost:8080/swagger-ui.html
```

---

## API Reference

### CV Upload

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/upload` | Upload a `.zip` file containing CV documents |

**Request:** `multipart/form-data` with field `file`

**Response:**
```json
{
  "success": true,
  "message": "Batch job started successfully",
  "totalFiles": 5,
  "jobStatus": "STARTING",
  "timestamp": "2026-05-24T10:00:00Z"
}
```

---

### Candidates

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/candidates` | List candidates with optional filters and pagination |
| `GET` | `/api/candidates/{id}` | Retrieve a single candidate by ID |
| `GET` | `/api/candidates/count` | Total number of candidates in the system |

**Filter parameters for `GET /api/candidates`:**

| Parameter | Type | Description | Example |
|---|---|---|---|
| `skill` | `String` | Filter by technical skill | `?skill=Java` |
| `minExperience` | `Integer` | Minimum years of experience | `?minExperience=3` |
| `location` | `String` | Preferred location (partial match) | `?location=Baku` |
| `jobType` | `Enum` | `REMOTE`, `HYBRID`, `ONSITE` | `?jobType=REMOTE` |
| `status` | `Enum` | `SUCCESS`, `FAILED`, `SKIPPED` | `?status=SUCCESS` |
| `page` | `Integer` | Page number (0-based) | `?page=0&size=10` |
| `sort` | `String` | Sort field and direction | `?sort=createdAt,desc` |

---

### Export

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/candidates/export/csv` | Download all parsed candidates as CSV |
| `GET` | `/api/candidates/export/xlsx` | Download all parsed candidates as Excel |

---

### Job Monitoring

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/jobs/status` | Status and statistics of the most recent batch job |
| `GET` | `/api/jobs/history` | Execution history of all batch jobs |

**Response for `/api/jobs/status`:**
```json
{
  "jobName": "cvProcessingJob",
  "status": "COMPLETED",
  "startTime": "2026-05-24T10:00:00Z",
  "endTime": "2026-05-24T10:00:05Z",
  "totalFiles": 5,
  "successCount": 5,
  "failedCount": 0,
  "skippedCount": 0,
  "duration": "5 seconds"
}
```

---

## Project Structure

```
src/
├── main/
│   ├── java/com/cvscanner/cv_scanner/
│   │   ├── batch/
│   │   │   ├── BatchConfig.java           # Job, Step, chunk configuration
│   │   │   ├── CvItemReader.java          # Reads CV files from temp directory
│   │   │   ├── CvItemProcessor.java       # File → Candidate (Tika + Parser)
│   │   │   ├── CvItemWriter.java          # Persists Candidate entities to DB
│   │   │   ├── CvSkipListener.java        # Logs skipped files
│   │   │   └── JobCompletionListener.java # Cleanup + email notification
│   │   ├── controller/
│   │   │   ├── UploadController.java
│   │   │   ├── CandidateController.java
│   │   │   ├── ExportController.java
│   │   │   └── JobController.java
│   │   ├── service/
│   │   │   ├── CvUploadService.java       # Upload orchestration
│   │   │   ├── CandidateService.java      # Candidate query logic
│   │   │   ├── ExportService.java         # CSV/Excel generation
│   │   │   ├── JobService.java            # Batch job monitoring
│   │   │   ├── TikaService.java           # Text extraction
│   │   │   ├── CvParserService.java       # Regex-based data parsing
│   │   │   ├── FileStorageService.java    # ZIP extraction and cleanup
│   │   │   ├── FileValidationService.java # MIME type validation
│   │   │   └── NotificationService.java   # Email sending
│   │   ├── entity/
│   │   │   ├── BaseEntity.java            # Audit fields (createdAt, updatedAt)
│   │   │   └── Candidate.java
│   │   ├── repository/
│   │   │   └── CandidateRepository.java
│   │   ├── specification/
│   │   │   └── CandidateSpec.java         # Dynamic JPA filter
│   │   ├── dto/
│   │   │   ├── UploadResponse.java
│   │   │   ├── CandidateResponse.java
│   │   │   └── JobStatusResponse.java
│   │   └── enums/
│   │       ├── JobType.java               # REMOTE, HYBRID, ONSITE
│   │       └── ProcessingStatus.java      # SUCCESS, FAILED, SKIPPED
│   └── resources/
│       ├── db/migration/
│       │   ├── V1__create_candidates_table.sql
│       │   └── V2__add_audit_columns.sql
│       └── application.properties
```

---

## Configuration

### Environment Variables

| Variable | Description |
|---|---|
| `DB_PASSWORD` | PostgreSQL database password |
| `MAIL_USERNAME` | Gmail address used for sending notifications |
| `MAIL_PASSWORD` | Gmail App Password (not your account password) |

### Docker Services

| Service | Host Port | Description |
|---|---|---|
| PostgreSQL 16 | `5437` | Primary database |
| pgAdmin 4 | `5050` | Database management UI (`admin@cvscanner.com` / `admin123`) |

### Supported File Formats

| Format | Extension |
|---|---|
| PDF | `.pdf` |
| Word Document | `.docx` |

---

## Bonus Features

### Retry / Skip Strategy

Configured in `BatchConfig` via Spring Batch's fault-tolerant step:

- **Retry:** Each file is retried up to **3 times** before being marked as failed
- **Skip:** Up to **50 files** can be skipped per job run
- **Skip logging:** `CvSkipListener` logs every skipped file with the reason

### Email Notification

After each batch job completes, an HTML email is sent to the configured HR address containing:

- Job status (COMPLETED / FAILED)
- Number of successfully parsed CVs
- Number of failed and skipped CVs
- Total execution duration

### Filterable Candidate API

Multi-parameter filtering with pagination:

```
GET /api/candidates?skill=Java&minExperience=5&jobType=REMOTE&page=0&size=20
```

Returns only candidates matching **all** supplied criteria, sorted by `createdAt` descending by default.