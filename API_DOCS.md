# BookStack API Documentation

This document provides detailed API documentation for the BookStack backend system, including request/response examples for all endpoints.

## Base URL

```
http://localhost:8080
```

## Interactive Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## Error Response Format

All error responses follow this format:

```json
{
  "error": "Error message description",
  "details": ["Optional array of detailed error messages"],
  "timestamp": "2026-01-15T10:30:00"
}
```

### Common HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| 200 | OK - Request succeeded |
| 201 | Created - Resource successfully created |
| 204 | No Content - Request succeeded with no response body |
| 400 | Bad Request - Validation error or business rule violation |
| 404 | Not Found - Resource not found |
| 500 | Internal Server Error - Unexpected server error |

---

## Member API

### POST /api/members

Register a new member.

**Request:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+90 555 123 4567"
}
```

**Response (201 Created):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "membershipNumber": "MBR-ABC12345",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+90 555 123 4567",
  "status": "ACTIVE"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Email already exists: john.doe@example.com",
  "timestamp": "2026-01-15T10:30:00"
}
```

---

### GET /api/members/{id}

Get member by ID.

**Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "membershipNumber": "MBR-ABC12345",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+90 555 123 4567",
  "status": "ACTIVE",
  "maxActiveLoans": 5
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "Member not found: 123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2026-01-15T10:30:00"
}
```

---

### GET /api/members

List all members.

**Response (200 OK):**
```json
{
  "members": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "membershipNumber": "MBR-ABC12345",
      "fullName": "John Doe",
      "email": "john.doe@example.com",
      "status": "ACTIVE"
    },
    {
      "id": "223e4567-e89b-12d3-a456-426614174001",
      "membershipNumber": "MBR-DEF67890",
      "fullName": "Jane Smith",
      "email": "jane.smith@example.com",
      "status": "ACTIVE"
    }
  ]
}
```

---

### PUT /api/members/{id}

Update member details.

**Request:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.updated@example.com",
  "phone": "+90 555 999 8888"
}
```

**Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "membershipNumber": "MBR-ABC12345",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.updated@example.com",
  "phone": "+90 555 999 8888",
  "status": "ACTIVE"
}
```

---

### DELETE /api/members/{id}

Delete a member.

**Response (204 No Content):**
No response body.

---

### PATCH /api/members/{id}/suspend

Suspend a member.

**Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "membershipNumber": "MBR-ABC12345",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "status": "SUSPENDED"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Invalid status transition from SUSPENDED to SUSPENDED",
  "timestamp": "2026-01-15T10:30:00"
}
```

---

### PATCH /api/members/{id}/activate

Activate a suspended or expired member.

**Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "membershipNumber": "MBR-ABC12345",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "status": "ACTIVE"
}
```

---

## Book API

### POST /api/books

Add a new book to the catalog.

**Request:**
```json
{
  "isbn": "978-3-16-148410-0",
  "title": "Clean Code",
  "authors": ["Robert C. Martin"],
  "categories": ["Programming", "Software Engineering"],
  "description": "A handbook of agile software craftsmanship",
  "publishedYear": 2008
}
```

**Response (201 Created):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "isbn": "978-3-16-148410-0",
  "title": "Clean Code",
  "authors": ["Robert C. Martin"],
  "categories": ["Programming", "Software Engineering"],
  "description": "A handbook of agile software craftsmanship",
  "publishedYear": 2008
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "ISBN already exists: 978-3-16-148410-0",
  "timestamp": "2026-01-15T10:30:00"
}
```

---

### GET /api/books/{id}

Get book by ID.

**Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "isbn": "978-3-16-148410-0",
  "title": "Clean Code",
  "authors": ["Robert C. Martin"],
  "categories": ["Programming", "Software Engineering"],
  "description": "A handbook of agile software craftsmanship",
  "publishedYear": 2008,
  "createdAt": "2026-01-15T10:30:00"
}
```

---

### GET /api/books

List all books.

**Response (200 OK):**
```json
{
  "books": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "isbn": "978-3-16-148410-0",
      "title": "Clean Code",
      "authors": ["Robert C. Martin"],
      "categories": ["Programming"],
      "publishedYear": 2008
    },
    {
      "id": "223e4567-e89b-12d3-a456-426614174001",
      "isbn": "978-0-13-468599-1",
      "title": "The Pragmatic Programmer",
      "authors": ["David Thomas", "Andrew Hunt"],
      "categories": ["Programming"],
      "publishedYear": 2019
    }
  ]
}
```

---

### GET /api/books/search?q={query}

Search books by title or ISBN.

**Example:** `GET /api/books/search?q=Clean`

**Response (200 OK):**
```json
{
  "books": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "isbn": "978-3-16-148410-0",
      "title": "Clean Code",
      "authors": ["Robert C. Martin"],
      "categories": ["Programming"],
      "publishedYear": 2008
    }
  ]
}
```

---

### PUT /api/books/{id}

Update book details.

**Request:**
```json
{
  "title": "Clean Code: A Handbook",
  "authors": ["Robert C. Martin"],
  "categories": ["Programming", "Best Practices"],
  "description": "Updated description",
  "publishedYear": 2008
}
```

**Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "isbn": "978-3-16-148410-0",
  "title": "Clean Code: A Handbook",
  "authors": ["Robert C. Martin"],
  "categories": ["Programming", "Best Practices"],
  "description": "Updated description",
  "publishedYear": 2008
}
```

---

### DELETE /api/books/{id}

Delete a book.

**Response (204 No Content):**
No response body.

---

## Book Copy API

### POST /api/copies

Add a new physical copy of a book.

**Request:**
```json
{
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "barcode": "BC-001-2026",
  "usageType": "BOTH"
}
```

**Usage Types:** `READING_ROOM_ONLY`, `BORROWABLE`, `BOTH`

**Response (201 Created):**
```json
{
  "id": "323e4567-e89b-12d3-a456-426614174002",
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "bookTitle": "Clean Code",
  "barcode": "BC-001-2026",
  "usageType": "BOTH",
  "status": "AVAILABLE"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Barcode already exists: BC-001-2026",
  "timestamp": "2026-01-15T10:30:00"
}
```

---

### GET /api/copies/{id}

Get copy by ID.

**Response (200 OK):**
```json
{
  "id": "323e4567-e89b-12d3-a456-426614174002",
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "bookTitle": "Clean Code",
  "bookIsbn": "978-3-16-148410-0",
  "barcode": "BC-001-2026",
  "usageType": "BOTH",
  "status": "AVAILABLE",
  "createdAt": "2026-01-15T10:30:00"
}
```

**Copy Statuses:** `AVAILABLE`, `LOANED`, `ON_HOLD`, `DAMAGED`, `LOST`

---

### GET /api/copies

List all copies.

**Response (200 OK):**
```json
{
  "copies": [
    {
      "id": "323e4567-e89b-12d3-a456-426614174002",
      "bookId": "123e4567-e89b-12d3-a456-426614174000",
      "bookTitle": "Clean Code",
      "barcode": "BC-001-2026",
      "usageType": "BOTH",
      "status": "AVAILABLE"
    }
  ]
}
```

---

### GET /api/books/{bookId}/copies

List all copies of a specific book.

**Response (200 OK):**
```json
{
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "bookTitle": "Clean Code",
  "copies": [
    {
      "id": "323e4567-e89b-12d3-a456-426614174002",
      "barcode": "BC-001-2026",
      "usageType": "BOTH",
      "status": "AVAILABLE"
    },
    {
      "id": "423e4567-e89b-12d3-a456-426614174003",
      "barcode": "BC-002-2026",
      "usageType": "READING_ROOM_ONLY",
      "status": "AVAILABLE"
    }
  ]
}
```

---

### PUT /api/copies/{id}

Update copy details.

**Request:**
```json
{
  "usageType": "BORROWABLE",
  "status": "AVAILABLE"
}
```

**Response (200 OK):**
```json
{
  "id": "323e4567-e89b-12d3-a456-426614174002",
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "bookTitle": "Clean Code",
  "barcode": "BC-001-2026",
  "usageType": "BORROWABLE",
  "status": "AVAILABLE"
}
```

---

### DELETE /api/copies/{id}

Delete a copy.

**Response (204 No Content):**
No response body.

---

## Visit API

### POST /api/visits/checkin

Check-in a member to the library.

**Request:**
```json
{
  "memberId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Response (201 Created):**
```json
{
  "id": "523e4567-e89b-12d3-a456-426614174004",
  "memberId": "123e4567-e89b-12d3-a456-426614174000",
  "memberName": "John Doe",
  "checkInTime": "2026-01-15T10:30:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Member is not active: SUSPENDED",
  "timestamp": "2026-01-15T10:30:00"
}
```

```json
{
  "error": "Member already has an active visit",
  "timestamp": "2026-01-15T10:30:00"
}
```

---

### POST /api/visits/{id}/checkout

Check-out a member from the library.

**Response (200 OK):**
```json
{
  "id": "523e4567-e89b-12d3-a456-426614174004",
  "memberId": "123e4567-e89b-12d3-a456-426614174000",
  "memberName": "John Doe",
  "checkInTime": "2026-01-15T10:30:00",
  "checkOutTime": "2026-01-15T14:30:00"
}
```

---

### GET /api/visits/{id}

Get visit by ID.

**Response (200 OK):**
```json
{
  "id": "523e4567-e89b-12d3-a456-426614174004",
  "memberId": "123e4567-e89b-12d3-a456-426614174000",
  "memberName": "John Doe",
  "membershipNumber": "MBR-ABC12345",
  "checkInTime": "2026-01-15T10:30:00",
  "checkOutTime": null,
  "isActive": true
}
```

---

### GET /api/members/{memberId}/visits/active

Get member's active visit.

**Response (200 OK):**
```json
{
  "id": "523e4567-e89b-12d3-a456-426614174004",
  "memberId": "123e4567-e89b-12d3-a456-426614174000",
  "memberName": "John Doe",
  "checkInTime": "2026-01-15T10:30:00"
}
```

**Response when no active visit (200 OK):**
```json
null
```

---

### GET /api/members/{memberId}/visits

Get member's visit history.

**Response (200 OK):**
```json
{
  "visits": [
    {
      "id": "523e4567-e89b-12d3-a456-426614174004",
      "checkInTime": "2026-01-15T10:30:00",
      "checkOutTime": "2026-01-15T14:30:00",
      "isActive": false
    },
    {
      "id": "623e4567-e89b-12d3-a456-426614174005",
      "checkInTime": "2026-01-14T09:00:00",
      "checkOutTime": "2026-01-14T17:00:00",
      "isActive": false
    }
  ]
}
```

---

## Loan API

### POST /api/loans

Borrow a book copy.

**Request:**
```json
{
  "memberId": "123e4567-e89b-12d3-a456-426614174000",
  "copyId": "323e4567-e89b-12d3-a456-426614174002"
}
```

**Response (201 Created):**
```json
{
  "id": "723e4567-e89b-12d3-a456-426614174006",
  "memberId": "123e4567-e89b-12d3-a456-426614174000",
  "memberName": "John Doe",
  "copyId": "323e4567-e89b-12d3-a456-426614174002",
  "bookTitle": "Clean Code",
  "barcode": "BC-001-2026",
  "borrowedAt": "2026-01-15T10:30:00",
  "dueDate": "2026-01-29T10:30:00",
  "status": "ACTIVE"
}
```

**Error Responses (400 Bad Request):**
```json
{
  "error": "Member must be checked in to borrow a copy",
  "timestamp": "2026-01-15T10:30:00"
}
```

```json
{
  "error": "Copy is not available: LOANED",
  "timestamp": "2026-01-15T10:30:00"
}
```

```json
{
  "error": "Member has reached maximum active loans limit: 5",
  "timestamp": "2026-01-15T10:30:00"
}
```

```json
{
  "error": "Member has unpaid penalties ($15.00) above blocking threshold ($10.00)",
  "timestamp": "2026-01-15T10:30:00"
}
```

---

### POST /api/loans/{id}/return

Return a borrowed copy.

**Response (200 OK) - On-time return:**
```json
{
  "id": "723e4567-e89b-12d3-a456-426614174006",
  "memberId": "123e4567-e89b-12d3-a456-426614174000",
  "memberName": "John Doe",
  "copyId": "323e4567-e89b-12d3-a456-426614174002",
  "bookTitle": "Clean Code",
  "barcode": "BC-001-2026",
  "borrowedAt": "2026-01-15T10:30:00",
  "dueDate": "2026-01-29T10:30:00",
  "returnedAt": "2026-01-28T14:30:00",
  "status": "RETURNED",
  "isOverdue": false,
  "daysOverdue": null,
  "penaltyId": null,
  "penaltyAmount": null,
  "reservationAssigned": false,
  "reservationId": null
}
```

**Response (200 OK) - Late return with penalty:**
```json
{
  "id": "723e4567-e89b-12d3-a456-426614174006",
  "memberId": "123e4567-e89b-12d3-a456-426614174000",
  "memberName": "John Doe",
  "copyId": "323e4567-e89b-12d3-a456-426614174002",
  "bookTitle": "Clean Code",
  "barcode": "BC-001-2026",
  "borrowedAt": "2026-01-15T10:30:00",
  "dueDate": "2026-01-29T10:30:00",
  "returnedAt": "2026-02-01T14:30:00",
  "status": "RETURNED",
  "isOverdue": true,
  "daysOverdue": 3,
  "penaltyId": "823e4567-e89b-12d3-a456-426614174007",
  "penaltyAmount": 3.00,
  "reservationAssigned": false,
  "reservationId": null
}
```

**Response (200 OK) - Return with reservation assignment:**
```json
{
  "id": "723e4567-e89b-12d3-a456-426614174006",
  "memberId": "123e4567-e89b-12d3-a456-426614174000",
  "memberName": "John Doe",
  "copyId": "323e4567-e89b-12d3-a456-426614174002",
  "bookTitle": "Clean Code",
  "barcode": "BC-001-2026",
  "borrowedAt": "2026-01-15T10:30:00",
  "dueDate": "2026-01-29T10:30:00",
  "returnedAt": "2026-01-28T14:30:00",
  "status": "RETURNED",
  "isOverdue": false,
  "daysOverdue": null,
  "penaltyId": null,
  "penaltyAmount": null,
  "reservationAssigned": true,
  "reservationId": "923e4567-e89b-12d3-a456-426614174008"
}
```

---

### GET /api/loans/{id}

Get loan by ID.

**Response (200 OK):**
```json
{
  "id": "723e4567-e89b-12d3-a456-426614174006",
  "memberId": "123e4567-e89b-12d3-a456-426614174000",
  "memberName": "John Doe",
  "membershipNumber": "MBR-ABC12345",
  "copyId": "323e4567-e89b-12d3-a456-426614174002",
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "bookTitle": "Clean Code",
  "bookIsbn": "978-3-16-148410-0",
  "barcode": "BC-001-2026",
  "borrowedAt": "2026-01-15T10:30:00",
  "dueDate": "2026-01-29T10:30:00",
  "returnedAt": null,
  "status": "ACTIVE",
  "isOverdue": false
}
```

---

### GET /api/members/{memberId}/loans/active

Get member's active loans.

**Response (200 OK):**
```json
{
  "loans": [
    {
      "id": "723e4567-e89b-12d3-a456-426614174006",
      "copyId": "323e4567-e89b-12d3-a456-426614174002",
      "bookTitle": "Clean Code",
      "bookIsbn": "978-3-16-148410-0",
      "barcode": "BC-001-2026",
      "borrowedAt": "2026-01-15T10:30:00",
      "dueDate": "2026-01-29T10:30:00",
      "isOverdue": false
    }
  ]
}
```

---

### GET /api/members/{memberId}/loans

Get member's loan history.

**Response (200 OK):**
```json
{
  "loans": [
    {
      "id": "723e4567-e89b-12d3-a456-426614174006",
      "copyId": "323e4567-e89b-12d3-a456-426614174002",
      "bookTitle": "Clean Code",
      "bookIsbn": "978-3-16-148410-0",
      "barcode": "BC-001-2026",
      "borrowedAt": "2026-01-15T10:30:00",
      "dueDate": "2026-01-29T10:30:00",
      "returnedAt": "2026-01-28T14:30:00",
      "status": "RETURNED",
      "isOverdue": false
    },
    {
      "id": "823e4567-e89b-12d3-a456-426614174007",
      "copyId": "423e4567-e89b-12d3-a456-426614174003",
      "bookTitle": "The Pragmatic Programmer",
      "bookIsbn": "978-0-13-468599-1",
      "barcode": "BC-002-2026",
      "borrowedAt": "2026-01-10T09:00:00",
      "dueDate": "2026-01-24T09:00:00",
      "returnedAt": null,
      "status": "ACTIVE",
      "isOverdue": true
    }
  ]
}
```

---

## Penalty API

### GET /api/penalties

List all penalties.

**Response (200 OK):**
```json
{
  "penalties": [
    {
      "id": "923e4567-e89b-12d3-a456-426614174008",
      "memberId": "123e4567-e89b-12d3-a456-426614174000",
      "memberName": "John Doe",
      "membershipNumber": "MBR-ABC12345",
      "bookTitle": "Clean Code",
      "barcode": "BC-001-2026",
      "amount": 5.00,
      "daysOverdue": 5,
      "status": "UNPAID",
      "createdAt": "2026-01-30T14:30:00"
    }
  ]
}
```

**Penalty Statuses:** `UNPAID`, `PAID`, `WAIVED`

---

### GET /api/penalties/{id}

Get penalty by ID.

**Response (200 OK):**
```json
{
  "id": "923e4567-e89b-12d3-a456-426614174008",
  "memberId": "123e4567-e89b-12d3-a456-426614174000",
  "memberName": "John Doe",
  "loanId": "723e4567-e89b-12d3-a456-426614174006",
  "bookTitle": "Clean Code",
  "barcode": "BC-001-2026",
  "amount": 5.00,
  "daysOverdue": 5,
  "status": "UNPAID",
  "paidAt": null,
  "createdAt": "2026-01-30T14:30:00"
}
```

---

### GET /api/members/{memberId}/penalties

Get member's penalties.

**Response (200 OK):**
```json
{
  "totalUnpaidAmount": 8.00,
  "penalties": [
    {
      "id": "923e4567-e89b-12d3-a456-426614174008",
      "loanId": "723e4567-e89b-12d3-a456-426614174006",
      "bookTitle": "Clean Code",
      "barcode": "BC-001-2026",
      "amount": 5.00,
      "daysOverdue": 5,
      "status": "UNPAID",
      "paidAt": null,
      "createdAt": "2026-01-30T14:30:00"
    },
    {
      "id": "a23e4567-e89b-12d3-a456-426614174009",
      "loanId": "823e4567-e89b-12d3-a456-426614174007",
      "bookTitle": "The Pragmatic Programmer",
      "barcode": "BC-002-2026",
      "amount": 3.00,
      "daysOverdue": 3,
      "status": "UNPAID",
      "paidAt": null,
      "createdAt": "2026-01-28T10:00:00"
    }
  ]
}
```

---

### POST /api/penalties/{id}/pay

Pay a penalty.

**Response (200 OK):**
```json
{
  "id": "923e4567-e89b-12d3-a456-426614174008",
  "memberId": "123e4567-e89b-12d3-a456-426614174000",
  "memberName": "John Doe",
  "amount": 5.00,
  "status": "PAID",
  "paidAt": "2026-01-31T11:00:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Penalty is already paid",
  "timestamp": "2026-01-31T11:00:00"
}
```

---

## Reservation API

### POST /api/reservations

Create a new reservation.

**Request:**
```json
{
  "memberId": "123e4567-e89b-12d3-a456-426614174000",
  "bookId": "223e4567-e89b-12d3-a456-426614174001"
}
```

**Response (201 Created):**
```json
{
  "id": "b23e4567-e89b-12d3-a456-426614174010",
  "memberId": "123e4567-e89b-12d3-a456-426614174000",
  "memberName": "John Doe",
  "bookId": "223e4567-e89b-12d3-a456-426614174001",
  "bookTitle": "The Pragmatic Programmer",
  "queuePosition": 1,
  "status": "WAITING",
  "createdAt": "2026-01-15T10:30:00"
}
```

**Error Responses (400 Bad Request):**
```json
{
  "error": "Member is not active: SUSPENDED",
  "timestamp": "2026-01-15T10:30:00"
}
```

```json
{
  "error": "Member already has an active reservation for this book",
  "timestamp": "2026-01-15T10:30:00"
}
```

---

### GET /api/reservations/{id}

Get reservation by ID.

**Response (200 OK):**
```json
{
  "id": "b23e4567-e89b-12d3-a456-426614174010",
  "memberId": "123e4567-e89b-12d3-a456-426614174000",
  "memberName": "John Doe",
  "bookId": "223e4567-e89b-12d3-a456-426614174001",
  "bookTitle": "The Pragmatic Programmer",
  "copyId": "323e4567-e89b-12d3-a456-426614174002",
  "barcode": "BC-002-2026",
  "queuePosition": 1,
  "status": "READY_FOR_PICKUP",
  "notifiedAt": "2026-01-16T09:00:00",
  "expiresAt": "2026-01-19T09:00:00",
  "createdAt": "2026-01-15T10:30:00"
}
```

**Reservation Statuses:** `WAITING`, `READY_FOR_PICKUP`, `FULFILLED`, `CANCELLED`, `EXPIRED`

---

### GET /api/reservations

List all reservations.

**Response (200 OK):**
```json
{
  "reservations": [
    {
      "id": "b23e4567-e89b-12d3-a456-426614174010",
      "memberId": "123e4567-e89b-12d3-a456-426614174000",
      "memberName": "John Doe",
      "membershipNumber": "MBR-ABC12345",
      "bookId": "223e4567-e89b-12d3-a456-426614174001",
      "bookTitle": "The Pragmatic Programmer",
      "queuePosition": 1,
      "status": "WAITING",
      "expiresAt": null,
      "createdAt": "2026-01-15T10:30:00"
    }
  ]
}
```

---

### DELETE /api/reservations/{id}

Cancel a reservation.

**Response (204 No Content):**
No response body.

**Error Response (400 Bad Request):**
```json
{
  "error": "Reservation cannot be cancelled: FULFILLED",
  "timestamp": "2026-01-15T10:30:00"
}
```

---

### GET /api/members/{memberId}/reservations

Get member's active reservations.

**Response (200 OK):**
```json
{
  "reservations": [
    {
      "id": "b23e4567-e89b-12d3-a456-426614174010",
      "bookId": "223e4567-e89b-12d3-a456-426614174001",
      "bookTitle": "The Pragmatic Programmer",
      "isbn": "978-0-13-468599-1",
      "queuePosition": 1,
      "status": "WAITING",
      "expiresAt": null,
      "createdAt": "2026-01-15T10:30:00"
    }
  ]
}
```

---

### GET /api/books/{bookId}/reservations

Get reservation queue for a book.

**Response (200 OK):**
```json
{
  "bookId": "223e4567-e89b-12d3-a456-426614174001",
  "bookTitle": "The Pragmatic Programmer",
  "totalWaiting": 3,
  "queue": [
    {
      "id": "b23e4567-e89b-12d3-a456-426614174010",
      "memberId": "123e4567-e89b-12d3-a456-426614174000",
      "memberName": "John Doe",
      "membershipNumber": "MBR-ABC12345",
      "queuePosition": 1,
      "status": "READY_FOR_PICKUP",
      "expiresAt": "2026-01-19T09:00:00",
      "createdAt": "2026-01-15T10:30:00"
    },
    {
      "id": "c23e4567-e89b-12d3-a456-426614174011",
      "memberId": "223e4567-e89b-12d3-a456-426614174001",
      "memberName": "Jane Smith",
      "membershipNumber": "MBR-DEF67890",
      "queuePosition": 2,
      "status": "WAITING",
      "expiresAt": null,
      "createdAt": "2026-01-15T11:00:00"
    },
    {
      "id": "d23e4567-e89b-12d3-a456-426614174012",
      "memberId": "323e4567-e89b-12d3-a456-426614174002",
      "memberName": "Bob Wilson",
      "membershipNumber": "MBR-GHI11111",
      "queuePosition": 3,
      "status": "WAITING",
      "expiresAt": null,
      "createdAt": "2026-01-15T11:30:00"
    }
  ]
}
```

---

## Enums Reference

### MemberStatus
- `ACTIVE` - Member can use all library services
- `SUSPENDED` - Member is blocked from all operations
- `EXPIRED` - Membership has expired, can be reactivated

### CopyStatus
- `AVAILABLE` - Copy is available for borrowing or reading
- `LOANED` - Copy is currently borrowed
- `ON_HOLD` - Copy is reserved for a member
- `DAMAGED` - Copy is damaged
- `LOST` - Copy is lost

### UsageType
- `READING_ROOM_ONLY` - Can only be used inside the library
- `BORROWABLE` - Can be borrowed
- `BOTH` - Can be used both ways

### LoanStatus
- `ACTIVE` - Loan is ongoing
- `RETURNED` - Copy has been returned

### PenaltyStatus
- `UNPAID` - Penalty has not been paid
- `PAID` - Penalty has been paid
- `WAIVED` - Penalty has been waived

### ReservationStatus
- `WAITING` - Waiting in queue for a copy
- `READY_FOR_PICKUP` - Copy assigned, ready for pickup
- `FULFILLED` - Reservation completed (copy borrowed)
- `CANCELLED` - Reservation cancelled by member or admin
- `EXPIRED` - Pickup window expired
