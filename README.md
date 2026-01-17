# BookStack

[![CI](https://github.com/muratcant/BookStack/actions/workflows/ci.yml/badge.svg)](https://github.com/muratcant/BookStack/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/muratcant/BookStack/graph/badge.svg?token=NPTT1UYWC7)](https://codecov.io/gh/muratcant/BookStack)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-purple?logo=kotlin)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-green?logo=spring)

> Hybrid Bookstore & Reading Library Backend System

BookStack is a backend system that combines bookstore and reading library functionalities. It is developed with Spring Boot + Kotlin using **Vertical Slice Architecture**.

## Tech Stack

| Technology | Version |
|------------|---------|
| Kotlin | 2.2.21 |
| Spring Boot | 4.0.1 |
| Spring Data JPA | 4.0.1 |
| PostgreSQL | 16 |
| springdoc-openapi | 2.8.0 |
| Kotest | 5.9.1 |
| MockK | 1.13.13 |
| JaCoCo | 0.8.12 |
| Docker Compose | - |
| Gradle | 9.x |

## Getting Started

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
- Root (redirects to Swagger): http://localhost:8080/
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs
- Health: http://localhost:8080/actuator/health

## Testing

### Test Framework
- **Unit Tests**: Kotest + MockK (Given-When-Then style)
- **Integration Tests**: JUnit 5 + MockMvc + PostgreSQL (Docker Compose)
- **E2E Tests**: Full system scenarios with real database verification
- **Coverage**: JaCoCo (combined unit + integration, excludes shared package)

### Test Statistics
- **Unit Tests**: 80+ scenarios (handler logic)
- **Integration Tests**: 100+ API endpoint tests
- **E2E Tests**: 13 end-to-end business scenarios
  - 4 loan lifecycle scenarios
  - 4 reservation queue scenarios
  - 5 member restriction scenarios

### Run Tests

**Unit Tests:**
```bash
./gradlew test
```

**Integration Tests:**
```bash
./gradlew integrationTest
```
> Note: Integration tests automatically start/stop Docker Compose PostgreSQL on port 5433

**E2E Tests:**
```bash
./gradlew integrationTest --tests "*.e2e.*"
```

**All Tests:**
```bash
./gradlew check
```

**Generate Coverage Report:**
```bash
./gradlew jacocoFullReport
```

**Coverage Reports:**
- **Full (Unit + Integration)**: `build/reports/jacoco/full/html/index.html`
- **Integration Test**: `build/reports/jacoco/integrationTest/html/index.html`
- **Unit Test**: `build/reports/jacoco/test/html/index.html`

> Note: `shared` package is excluded from coverage reports (infrastructure code)

## Project Structure

```
src/main/kotlin/org/muratcant/bookstack/
├── shared/                     # Cross-cutting concerns
│   ├── config/                 # OpenApiConfig, WebConfig
│   ├── exception/              # GlobalExceptionHandler, custom exceptions
│   └── domain/                 # BaseEntity
├── features/                   # Feature modules (Vertical Slices)
│   ├── member/                 # Member management (CRUD, status transitions)
│   ├── book/                   # Book catalog (CRUD, search)
│   ├── bookcopy/               # Physical book copies (CRUD, status tracking)
│   ├── visit/                  # Check-in/check-out tracking
│   ├── loan/                   # Borrowing and returning logic
│   ├── penalty/                # Late return fees and payment
│   └── reservation/            # FIFO reservation queue system
└── BookStackApplication.kt     # Main application class

src/test/kotlin/                # Unit tests (Kotest + MockK)
src/integrationTest/kotlin/     # Integration & E2E tests
    ├── features/               # API Integration tests (MockMvc)
    └── e2e/                    # End-to-End system tests
```

## Architecture

BookStack uses **Vertical Slice Architecture (VSA)**:

- Each feature is a self-contained vertical slice
- Slices contain their own Controller, Handler, Request/Response DTOs
- Shared concerns are in the `shared` package
- No traditional layered architecture (no service/repository layers spanning features)

## API Documentation

For detailed API documentation with request/response examples, see [API_DOCS.md](API_DOCS.md).

### API Endpoints Overview

#### MemberController (`/api/members`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/members` | Register new member |
| `GET` | `/api/members/{id}` | Get member by ID |
| `GET` | `/api/members` | List all members |
| `PUT` | `/api/members/{id}` | Update member |
| `DELETE` | `/api/members/{id}` | Delete member |
| `PATCH` | `/api/members/{id}/suspend` | Suspend member |
| `PATCH` | `/api/members/{id}/activate` | Activate member |

#### BookController (`/api/books`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/books` | Add new book to catalog |
| `GET` | `/api/books/{id}` | Get book by ID |
| `GET` | `/api/books` | List all books |
| `GET` | `/api/books/search?q={query}` | Search books by title or ISBN |
| `PUT` | `/api/books/{id}` | Update book |
| `DELETE` | `/api/books/{id}` | Delete book |

#### BookCopyController (`/api/copies`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/copies` | Add new physical copy |
| `GET` | `/api/copies/{id}` | Get copy by ID |
| `GET` | `/api/copies` | List all copies |
| `GET` | `/api/books/{bookId}/copies` | List copies by book |
| `PUT` | `/api/copies/{id}` | Update copy |
| `DELETE` | `/api/copies/{id}` | Delete copy |

#### VisitController (`/api/visits`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/visits/checkin` | Check-in member |
| `POST` | `/api/visits/{id}/checkout` | Check-out member |
| `GET` | `/api/visits/{id}` | Get visit by ID |
| `GET` | `/api/members/{memberId}/visits/active` | Get member's active visit |
| `GET` | `/api/members/{memberId}/visits` | Get member's visit history |

#### LoanController (`/api/loans`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/loans` | Borrow copy |
| `POST` | `/api/loans/{id}/return` | Return copy |
| `GET` | `/api/loans/{id}` | Get loan by ID |
| `GET` | `/api/members/{memberId}/loans/active` | Get member's active loans |
| `GET` | `/api/members/{memberId}/loans` | Get member's loan history |

#### ReservationController (`/api/reservations`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/reservations` | Create reservation |
| `DELETE` | `/api/reservations/{id}` | Cancel reservation |
| `GET` | `/api/reservations/{id}` | Get reservation by ID |
| `GET` | `/api/reservations` | List all reservations |
| `GET` | `/api/members/{memberId}/reservations` | Get member's reservations |
| `GET` | `/api/books/{bookId}/reservations` | Get reservation queue for book |

#### PenaltyController (`/api/penalties`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/penalties` | List all penalties |
| `GET` | `/api/penalties/{id}` | Get penalty by ID |
| `POST` | `/api/penalties/{id}/pay` | Pay penalty |
| `GET` | `/api/members/{memberId}/penalties` | Get member's penalties |

## Configuration

Key configuration in `application.yml`:

```yaml
bookstack:
  loan:
    default-duration-days: 14      # Default loan duration
    max-extensions: 2               # Maximum loan extensions allowed
    extension-days: 7               # Days added per extension
  penalty:
    daily-fee: 1.00                 # Fee per day overdue
    blocking-threshold: 10.00       # Threshold to block borrowing
  reservation:
    pickup-window-days: 3           # Days to pickup reserved copy
```

## Business Rules

### Member Rules
- Email must be unique across all members
- Only ACTIVE members can check-in and borrow books
- SUSPENDED members are blocked from all operations
- EXPIRED members can be reactivated to ACTIVE status
- Status transitions: ACTIVE ↔ SUSPENDED, EXPIRED → ACTIVE

### Visit Rules
- Members must check-in before borrowing books
- Only one active visit per member at a time
- Concurrent check-in attempts are prevented
- Check-out closes the active visit

### Copy Rules
- Each copy must have a unique barcode
- Copy types:
  - **READING_ROOM_ONLY**: Can only be used inside library
  - **BORROWABLE**: Can be borrowed
  - **BOTH**: Can be used both ways
- Copy statuses: AVAILABLE, LOANED, ON_HOLD, DAMAGED, LOST

### Loan Rules
- Member must be checked-in to borrow
- Copy must be AVAILABLE and (BORROWABLE or BOTH)
- Maximum active loans per member: 5 (configurable)
- Default loan duration: 14 days (configurable)
- Overdue returns automatically create penalties
- Returns calculate overdue days and create penalties if needed

### Penalty Rules
- Automatically created when loan is overdue
- Daily fee: $1.00 (configurable)
- Blocking threshold: $10.00 (configurable)
- Members with unpaid penalties above threshold cannot borrow
- Payment changes penalty status to PAID
- Amount calculation: days overdue × daily fee

### Reservation Rules
- FIFO queue system (first-come, first-served)
- Only ACTIVE members can create reservations
- One reservation per member per book
- When copy becomes available:
  - Copy status changes to ON_HOLD
  - First waiting reservation becomes READY_FOR_PICKUP
- ON_HOLD copies can only be borrowed by reservation holder
- Pickup window: 3 days (configurable)
- Cancelled reservations update queue positions automatically

## Docker

### Development (DB only)
```bash
docker compose -f docker-compose.dev.yml up -d
```

### Full Stack (App + DB)
```bash
docker compose up -d --build
```

### View Logs
```bash
docker compose logs -f app
```

### Stop
```bash
docker compose down
```

## CI/CD

- **GitHub Actions**: Automated testing and coverage reporting
- **Codecov**: Code coverage tracking
- **Docker**: Multi-stage builds for production-ready images

## Development Status

### Completed Features

#### Infrastructure Setup
- Spring Boot 4.0.1, Kotlin 2.2.21, PostgreSQL 16
- Docker Compose for development and testing
- GitHub Actions CI/CD with Codecov integration
- JaCoCo test coverage reporting (excludes shared package)
- Vertical Slice Architecture implementation

#### Member Management
- **CRUD Operations**: Register, Get, List, Update, Delete
- **Status Management**: Suspend, Activate with validation
- **Business Rules**: 
  - Email uniqueness validation
  - Status transitions (ACTIVE↔SUSPENDED, EXPIRED→ACTIVE)
  - Only ACTIVE members can perform operations
- **Tests**: Unit tests (Kotest) + Integration tests (MockMvc)

#### Book Catalog
- **CRUD Operations**: Add, Get, List, Update, Delete
- **Search**: Full-text search by title or ISBN
- **Business Rules**:
  - ISBN uniqueness validation
  - Support for multiple authors and categories
- **Tests**: Full unit and integration coverage

#### Physical Book Copies
- **CRUD Operations**: Add, Get, List by book, Update, Delete
- **Status Tracking**: AVAILABLE, LOANED, ON_HOLD, DAMAGED, LOST
- **Usage Types**: READING_ROOM_ONLY, BORROWABLE, BOTH
- **Business Rules**:
  - Barcode uniqueness validation
  - Copy linked to book catalog
- **Tests**: Full unit and integration coverage

#### Visit Tracking
- **Operations**: Check-in, Check-out, Get active visit, History
- **Business Rules**:
  - Only ACTIVE members can check-in
  - One active visit per member (concurrent check-in prevention)
  - Check-in required for borrowing
- **Tests**: Unit, integration, and E2E tests

#### Loan Management
- **Operations**: Borrow, Return, Get active loans, Loan history
- **Business Rules**:
  - Active member check-in required
  - Max active loans per member (default: 5)
  - Loan duration: 14 days (configurable)
  - Overdue detection and penalty creation
  - Copy availability and usage type validation
- **Tests**: Full coverage with business rule scenarios

#### Penalty System
- **Operations**: List, Get by ID, Pay, Get by member
- **Automatic Creation**: Creates penalty on overdue returns
- **Business Rules**:
  - Daily fee: $1.00 (configurable)
  - Blocking threshold: $10.00 (configurable)
  - Unpaid penalties above threshold block borrowing
- **Tests**: Unit, integration, and E2E tests

#### Reservation System
- **Operations**: Create, Cancel, Get queue, List by member
- **FIFO Queue**: First-come, first-served ordering
- **Business Rules**:
  - Automatic copy assignment when available
  - ON_HOLD status for reserved copies
  - Pickup window: 3 days (configurable)
  - One reservation per member per book
  - Only reservation holder can borrow ON_HOLD copy
- **Tests**: Full coverage including queue management

### Test Coverage
- **193+ test scenarios** across all domains
- **13 E2E tests** covering complete business flows
- **Database verification** in all integration tests
- **Automated coverage reporting** via JaCoCo and Codecov
