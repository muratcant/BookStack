package org.muratcant.bookstack.features.bookcopy

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.muratcant.bookstack.BaseIntegrationTest
import org.muratcant.bookstack.features.book.domain.Book
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.bookcopy.domain.BookCopy
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.bookcopy.domain.UsageType
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Transactional
class BookCopyControllerApiIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var bookCopyRepository: BookCopyRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var loanRepository: LoanRepository

    private lateinit var testBook: Book

    @BeforeEach
    fun setup() {
        loanRepository.deleteAll()
        bookCopyRepository.deleteAll()
        bookRepository.deleteAll()
        testBook = bookRepository.save(
            Book(
                isbn = "978-0-13-235088-4",
                title = "Clean Code"
            )
        )
    }

    // ==================== POST /api/copies ====================

    @Test
    fun `Given valid request When POST api copies Then should return 201 and save to database`() {
        // Given
        val request = mapOf(
            "bookId" to testBook.id.toString(),
            "barcode" to "BC-001-2024",
            "usageType" to "BOTH"
        )

        // When & Then
        mockMvc.perform(
            post("/api/copies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.bookId").value(testBook.id.toString()))
            .andExpect(jsonPath("$.bookTitle").value("Clean Code"))
            .andExpect(jsonPath("$.barcode").value("BC-001-2024"))
            .andExpect(jsonPath("$.usageType").value("BOTH"))
            .andExpect(jsonPath("$.status").value("AVAILABLE"))

        // Verify DB
        val savedCopy = bookCopyRepository.findByBarcode("BC-001-2024")
        assertNotNull(savedCopy)
        assertEquals(testBook.id, savedCopy.book.id)
    }

    @Test
    fun `Given non-existing book When POST api copies Then should return 404`() {
        // Given
        val request = mapOf(
            "bookId" to UUID.randomUUID().toString(),
            "barcode" to "BC-001-2024",
            "usageType" to "BOTH"
        )

        // When & Then
        mockMvc.perform(
            post("/api/copies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Given duplicate barcode When POST api copies Then should return 400`() {
        // Given - First save a copy
        bookCopyRepository.save(BookCopy(book = testBook, barcode = "BC-EXISTING"))

        val request = mapOf(
            "bookId" to testBook.id.toString(),
            "barcode" to "BC-EXISTING",
            "usageType" to "BOTH"
        )

        // When & Then
        mockMvc.perform(
            post("/api/copies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
    }

    // ==================== GET /api/copies/{id} ====================

    @Test
    fun `Given existing copy When GET api copies id Then should return 200`() {
        // Given
        val copy = bookCopyRepository.save(
            BookCopy(book = testBook, barcode = "BC-GET-001")
        )

        // When & Then
        mockMvc.perform(get("/api/copies/${copy.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(copy.id.toString()))
            .andExpect(jsonPath("$.barcode").value("BC-GET-001"))
            .andExpect(jsonPath("$.bookTitle").value("Clean Code"))
    }

    @Test
    fun `Given non-existing copy When GET api copies id Then should return 404`() {
        // When & Then
        mockMvc.perform(get("/api/copies/${UUID.randomUUID()}"))
            .andExpect(status().isNotFound)
    }

    // ==================== GET /api/copies ====================

    @Test
    fun `Given multiple copies When GET api copies Then should return all`() {
        // Given
        bookCopyRepository.save(BookCopy(book = testBook, barcode = "BC-LIST-001"))
        bookCopyRepository.save(BookCopy(book = testBook, barcode = "BC-LIST-002"))

        // When & Then
        mockMvc.perform(get("/api/copies"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.copies.length()").value(2))
    }

    @Test
    fun `Given no copies When GET api copies Then should return empty list`() {
        // When & Then
        mockMvc.perform(get("/api/copies"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.copies.length()").value(0))
    }

    // ==================== GET /api/books/{bookId}/copies ====================

    @Test
    fun `Given book with copies When GET api books bookId copies Then should return copies`() {
        // Given
        bookCopyRepository.save(BookCopy(book = testBook, barcode = "BC-BYBOOK-001"))
        bookCopyRepository.save(BookCopy(book = testBook, barcode = "BC-BYBOOK-002"))

        // Create another book with a copy that shouldn't be returned
        val otherBook = bookRepository.save(Book(isbn = "978-OTHER", title = "Other Book"))
        bookCopyRepository.save(BookCopy(book = otherBook, barcode = "BC-OTHER-001"))

        // When & Then
        mockMvc.perform(get("/api/books/${testBook.id}/copies"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.bookId").value(testBook.id.toString()))
            .andExpect(jsonPath("$.bookTitle").value("Clean Code"))
            .andExpect(jsonPath("$.copies.length()").value(2))
    }

    @Test
    fun `Given non-existing book When GET api books bookId copies Then should return 404`() {
        // When & Then
        mockMvc.perform(get("/api/books/${UUID.randomUUID()}/copies"))
            .andExpect(status().isNotFound)
    }

    // ==================== PUT /api/copies/{id} ====================

    @Test
    fun `Given valid update When PUT api copies id Then should update in database`() {
        // Given
        val copy = bookCopyRepository.save(
            BookCopy(
                book = testBook,
                barcode = "BC-UPDATE-001",
                usageType = UsageType.BOTH,
                status = CopyStatus.AVAILABLE
            )
        )

        val updateRequest = mapOf(
            "usageType" to "READING_ROOM_ONLY",
            "status" to "DAMAGED"
        )

        // When & Then
        mockMvc.perform(
            put("/api/copies/${copy.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.usageType").value("READING_ROOM_ONLY"))
            .andExpect(jsonPath("$.status").value("DAMAGED"))
            .andExpect(jsonPath("$.barcode").value("BC-UPDATE-001")) // Barcode shouldn't change

        // Verify DB
        val updated = bookCopyRepository.findById(copy.id).orElse(null)
        assertNotNull(updated)
        assertEquals(UsageType.READING_ROOM_ONLY, updated.usageType)
        assertEquals(CopyStatus.DAMAGED, updated.status)
    }

    @Test
    fun `Given non-existing copy When PUT api copies id Then should return 404`() {
        // Given
        val updateRequest = mapOf(
            "usageType" to "BOTH",
            "status" to "AVAILABLE"
        )

        // When & Then
        mockMvc.perform(
            put("/api/copies/${UUID.randomUUID()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
    }

    // ==================== DELETE /api/copies/{id} ====================

    @Test
    fun `Given existing copy When DELETE api copies id Then should remove from database`() {
        // Given
        val copy = bookCopyRepository.save(
            BookCopy(book = testBook, barcode = "BC-DELETE-001")
        )

        // When & Then
        mockMvc.perform(delete("/api/copies/${copy.id}"))
            .andExpect(status().isNoContent)

        // Verify DB
        val deleted = bookCopyRepository.findById(copy.id).orElse(null)
        assertNull(deleted)
    }

    @Test
    fun `Given non-existing copy When DELETE api copies id Then should return 404`() {
        // When & Then
        mockMvc.perform(delete("/api/copies/${UUID.randomUUID()}"))
            .andExpect(status().isNotFound)
    }
}

