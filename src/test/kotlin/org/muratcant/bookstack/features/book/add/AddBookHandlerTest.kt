package org.muratcant.bookstack.features.book.add

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.shared.exception.DuplicateResourceException

class AddBookHandlerTest : FunSpec({

    val bookRepository = mockk<BookRepository>()
    val handler = AddBookHandler(bookRepository)

    beforeTest {
        clearMocks(bookRepository)
    }

    test("Given valid request When add book Then should save and return response") {
        // Given
        val request = AddBookRequest(
            isbn = "978-0-13-235088-4",
            title = "Clean Code",
            authors = listOf("Robert C. Martin"),
            categories = listOf("Programming"),
            description = "A handbook of agile software craftsmanship",
            publishedYear = 2008
        )

        every { bookRepository.existsByIsbn(request.isbn) } returns false
        every { bookRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(request)

        // Then
        response.id shouldNotBe null
        response.isbn shouldBe "978-0-13-235088-4"
        response.title shouldBe "Clean Code"
        response.authors shouldBe listOf("Robert C. Martin")
        response.categories shouldBe listOf("Programming")
        response.description shouldBe "A handbook of agile software craftsmanship"
        response.publishedYear shouldBe 2008
        verify(exactly = 1) { bookRepository.save(any()) }
    }

    test("Given duplicate ISBN When add book Then should throw DuplicateResourceException") {
        // Given
        val request = AddBookRequest(
            isbn = "978-0-13-235088-4",
            title = "Clean Code",
            authors = listOf("Robert C. Martin"),
            categories = emptyList(),
            description = null,
            publishedYear = null
        )

        every { bookRepository.existsByIsbn(request.isbn) } returns true

        // When & Then
        val exception = shouldThrow<DuplicateResourceException> {
            handler.handle(request)
        }

        exception.message shouldBe "Book with ISBN already exists: 978-0-13-235088-4"
        verify(exactly = 0) { bookRepository.save(any()) }
    }

    test("Given minimal request When add book Then should save with empty lists") {
        // Given
        val request = AddBookRequest(
            isbn = "978-0-13-235088-4",
            title = "Clean Code",
            authors = emptyList(),
            categories = emptyList(),
            description = null,
            publishedYear = null
        )

        every { bookRepository.existsByIsbn(request.isbn) } returns false
        every { bookRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(request)

        // Then
        response.authors shouldBe emptyList()
        response.categories shouldBe emptyList()
        response.description shouldBe null
        response.publishedYear shouldBe null
    }

    test("Given multiple authors When add book Then should preserve all authors") {
        // Given
        val request = AddBookRequest(
            isbn = "978-0-13-235088-4",
            title = "Design Patterns",
            authors = listOf("Erich Gamma", "Richard Helm", "Ralph Johnson", "John Vlissides"),
            categories = listOf("Programming"),
            description = null,
            publishedYear = 1994
        )

        every { bookRepository.existsByIsbn(request.isbn) } returns false
        every { bookRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(request)

        // Then
        response.authors.size shouldBe 4
        response.authors shouldBe listOf("Erich Gamma", "Richard Helm", "Ralph Johnson", "John Vlissides")
    }
})

