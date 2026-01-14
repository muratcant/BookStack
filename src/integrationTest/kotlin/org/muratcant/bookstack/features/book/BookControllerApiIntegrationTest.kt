package org.muratcant.bookstack.features.book

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.muratcant.bookstack.BaseIntegrationTest
import org.muratcant.bookstack.features.book.add.AddBookRequest
import org.muratcant.bookstack.features.book.domain.Book
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
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
class BookControllerApiIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var bookCopyRepository: BookCopyRepository

    @BeforeEach
    fun setup() {
        bookCopyRepository.deleteAll()
        bookRepository.deleteAll()
    }

    // ==================== POST /api/books ====================

    @Test
    fun `Given valid request When POST api books Then should return 201 and save to database`() {
        // Given
        val request = AddBookRequest(
            isbn = "978-0-13-235088-4",
            title = "Clean Code",
            authors = listOf("Robert C. Martin"),
            categories = listOf("Programming", "Best Practices"),
            description = "A Handbook of Agile Software Craftsmanship",
            publishedYear = 2008
        )

        // When & Then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.isbn").value("978-0-13-235088-4"))
            .andExpect(jsonPath("$.title").value("Clean Code"))
            .andExpect(jsonPath("$.authors[0]").value("Robert C. Martin"))
            .andExpect(jsonPath("$.categories[0]").value("Programming"))
            .andExpect(jsonPath("$.description").value("A Handbook of Agile Software Craftsmanship"))
            .andExpect(jsonPath("$.publishedYear").value(2008))

        // Verify DB
        val savedBook = bookRepository.findByIsbn("978-0-13-235088-4")
        assertNotNull(savedBook)
        assertEquals("Clean Code", savedBook.title)
    }

    @Test
    fun `Given minimal request When POST api books Then should return 201`() {
        // Given
        val request = mapOf(
            "isbn" to "978-0-13-235088-4",
            "title" to "Clean Code"
        )

        // When & Then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.isbn").value("978-0-13-235088-4"))
            .andExpect(jsonPath("$.title").value("Clean Code"))
            .andExpect(jsonPath("$.authors").isEmpty)
    }

    @Test
    fun `Given duplicate ISBN When POST api books Then should return 400`() {
        // Given - First save a book
        bookRepository.save(
            Book(
                isbn = "978-0-13-235088-4",
                title = "Existing Book"
            )
        )

        val request = AddBookRequest(
            isbn = "978-0-13-235088-4",
            title = "New Book",
            authors = emptyList(),
            categories = emptyList(),
            description = null,
            publishedYear = null
        )

        // When & Then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `Given blank title When POST api books Then should return 400`() {
        // Given
        val request = mapOf(
            "isbn" to "978-0-13-235088-4",
            "title" to ""
        )

        // When & Then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    // ==================== GET /api/books/{id} ====================

    @Test
    fun `Given existing book When GET api books id Then should return 200`() {
        // Given
        val book = bookRepository.save(
            Book(
                isbn = "978-0-13-235088-4",
                title = "Clean Code",
                authors = listOf("Robert C. Martin")
            )
        )

        // When & Then
        mockMvc.perform(get("/api/books/${book.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(book.id.toString()))
            .andExpect(jsonPath("$.isbn").value("978-0-13-235088-4"))
            .andExpect(jsonPath("$.title").value("Clean Code"))
    }

    @Test
    fun `Given non-existing book When GET api books id Then should return 404`() {
        // When & Then
        mockMvc.perform(get("/api/books/${UUID.randomUUID()}"))
            .andExpect(status().isNotFound)
    }

    // ==================== GET /api/books ====================

    @Test
    fun `Given multiple books When GET api books Then should return all`() {
        // Given
        bookRepository.save(Book(isbn = "978-0-13-235088-4", title = "Clean Code"))
        bookRepository.save(Book(isbn = "978-0-13-468599-1", title = "Clean Architecture"))

        // When & Then
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.books.length()").value(2))
    }

    @Test
    fun `Given no books When GET api books Then should return empty list`() {
        // When & Then
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.books.length()").value(0))
    }

    // ==================== GET /api/books/search ====================

    @Test
    fun `Given matching query When GET api books search Then should return matching books`() {
        // Given
        bookRepository.save(Book(isbn = "978-0-13-235088-4", title = "Clean Code"))
        bookRepository.save(Book(isbn = "978-0-13-468599-1", title = "Clean Architecture"))
        bookRepository.save(Book(isbn = "978-0-59-651798-1", title = "JavaScript Guide"))

        // When & Then
        mockMvc.perform(get("/api/books/search").param("q", "Clean"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.query").value("Clean"))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.books.length()").value(2))
    }

    @Test
    fun `Given ISBN query When GET api books search Then should find book`() {
        // Given
        bookRepository.save(Book(isbn = "978-0-13-235088-4", title = "Clean Code"))

        // When & Then
        mockMvc.perform(get("/api/books/search").param("q", "978-0-13-235088"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.books[0].isbn").value("978-0-13-235088-4"))
    }

    @Test
    fun `Given no matching query When GET api books search Then should return empty`() {
        // Given
        bookRepository.save(Book(isbn = "978-0-13-235088-4", title = "Clean Code"))

        // When & Then
        mockMvc.perform(get("/api/books/search").param("q", "NonExistent"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.books").isEmpty)
    }

    // ==================== PUT /api/books/{id} ====================

    @Test
    fun `Given valid update When PUT api books id Then should update in database`() {
        // Given
        val book = bookRepository.save(
            Book(
                isbn = "978-0-13-235088-4",
                title = "Clean Code",
                authors = listOf("Robert C. Martin")
            )
        )

        val updateRequest = mapOf(
            "title" to "Clean Code: Updated Edition",
            "authors" to listOf("Robert C. Martin", "New Author"),
            "categories" to listOf("Programming"),
            "description" to "Updated description",
            "publishedYear" to 2020
        )

        // When & Then
        mockMvc.perform(
            put("/api/books/${book.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Clean Code: Updated Edition"))
            .andExpect(jsonPath("$.authors[1]").value("New Author"))
            .andExpect(jsonPath("$.isbn").value("978-0-13-235088-4")) // ISBN should not change

        // Verify DB
        val updated = bookRepository.findById(book.id).orElse(null)
        assertNotNull(updated)
        assertEquals("Clean Code: Updated Edition", updated.title)
    }

    @Test
    fun `Given non-existing book When PUT api books id Then should return 404`() {
        // Given
        val updateRequest = mapOf(
            "title" to "Updated Title"
        )

        // When & Then
        mockMvc.perform(
            put("/api/books/${UUID.randomUUID()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
    }

    // ==================== DELETE /api/books/{id} ====================

    @Test
    fun `Given existing book When DELETE api books id Then should remove from database`() {
        // Given
        val book = bookRepository.save(
            Book(
                isbn = "978-0-13-235088-4",
                title = "To Delete"
            )
        )

        // When & Then
        mockMvc.perform(delete("/api/books/${book.id}"))
            .andExpect(status().isNoContent)

        // Verify DB
        val deleted = bookRepository.findById(book.id).orElse(null)
        assertNull(deleted)
    }

    @Test
    fun `Given non-existing book When DELETE api books id Then should return 404`() {
        // When & Then
        mockMvc.perform(delete("/api/books/${UUID.randomUUID()}"))
            .andExpect(status().isNotFound)
    }
}

