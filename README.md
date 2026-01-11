# 📚 BookStack

[![CI](https://github.com/muratcant/BookStack/actions/workflows/ci.yml/badge.svg)](https://github.com/muratcant/BookStack/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/muratcant/BookStack/graph/badge.svg?token=NPTT1UYWC7)](https://codecov.io/gh/muratcant/BookStack)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-purple?logo=kotlin)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-green?logo=spring)

> Hybrid Bookstore & Reading Library Backend System

BookStack, kitapçı ve okuma kütüphanesi işlevlerini birleştiren bir backend sistemidir. **Vertical Slice Architecture** kullanılarak Spring Boot + Kotlin ile geliştirilmektedir.

## 🛠 Tech Stack

| Technology | Version |
|------------|---------|
| Kotlin | 2.2.21 |
| Spring Boot | 4.0.1 |
| Spring Data JPA | - |
| PostgreSQL | 16 |
| springdoc-openapi | 2.8.0 |
| Kotest | 5.9.1 |
| MockK | 1.13.13 |
| Docker Compose | - |

## 🚀 Getting Started

### Prerequisites

- JDK 21+
- Docker & Docker Compose
- Gradle 9.x

### Local Development

1. **Start PostgreSQL:**
```bash
docker compose -f docker-compose.dev.yml up -d
```

2. **Run the application:**
```bash
./gradlew bootRun
```

3. **Access the API:**
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs
- Health: http://localhost:8080/actuator/health

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Integration Tests
```bash
./gradlew integrationTest
```

### All Tests with Coverage
```bash
./gradlew check jacocoTestReport
```

Coverage report: `build/reports/jacoco/test/html/index.html`

## 📁 Project Structure

```
src/main/kotlin/org/muratcant/bookstack/
├── shared/                     # Cross-cutting concerns
│   ├── config/                 # OpenApiConfig, JpaConfig
│   ├── exception/              # GlobalExceptionHandler, custom exceptions
│   └── domain/                 # BaseEntity
├── features/
│   ├── member/                 # Member management
│   ├── book/                   # Book catalog
│   ├── copy/                   # Physical copies
│   ├── visit/                  # Check-in/check-out
│   ├── loan/                   # Borrowing logic
│   ├── reservation/            # FIFO holds
│   └── penalty/                # Late fees
```

## 🏗 Architecture

BookStack uses **Vertical Slice Architecture (VSA)**:

- Each feature is a self-contained vertical slice
- Slices contain their own Controller, Handler, Request/Response DTOs
- Shared concerns are in the `shared` package
- No traditional layered architecture (no service/repository layers spanning features)

## 📋 API Resources

| Resource | Description |
|----------|-------------|
| `/api/members` | Member management (CRUD, status) |
| `/api/books` | Book catalog (CRUD) |
| `/api/copies` | Physical book copies (CRUD) |
| `/api/visits` | Check-in/check-out tracking |
| `/api/loans` | Borrowing and returning |
| `/api/reservations` | Book reservations (FIFO) |
| `/api/penalties` | Late return fees |

## 🔧 Configuration

Key configuration in `application.yml`:

```yaml
bookstack:
  loan:
    default-duration-days: 14
    max-extensions: 2
    extension-days: 7
  penalty:
    daily-fee: 1.00
    blocking-threshold: 10.00
  reservation:
    pickup-window-days: 3
```

## 🐳 Docker

### Development (DB only)
```bash
docker compose -f docker-compose.dev.yml up -d
```

### Full Stack
```bash
docker compose up -d
```

