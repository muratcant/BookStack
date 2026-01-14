package org.muratcant.bookstack.features.book.get

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.book.test.BookBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class GetBookHandlerTest : FunSpec({

    val bookRepository = mockk<BookRepository>()
    val handler = GetBookHandler(bookRepository)

    beforeTest {
        clearMocks(bookRepository)
    }

    test("Given existing book id When get book Then should return book response") {
        // Given
        val bookId = UUID.randomUUID()
        val book = BookBuilder.aBook(
            id = bookId,
            isbn = "978-0-13-235088-4",
            title = "Clean Code",
            authors = listOf("Robert C. Martin")
        )

        every { bookRepository.findById(bookId) } returns Optional.of(book)

        // When
        val response = handler.handle(bookId)

        // Then
        response.id shouldBe bookId
        response.isbn shouldBe "978-0-13-235088-4"
        response.title shouldBe "Clean Code"
        response.authors shouldBe listOf("Robert C. Martin")
    }

    test("Given non-existing book id When get book Then should throw ResourceNotFoundException") {
        // Given
        val bookId = UUID.randomUUID()

        every { bookRepository.findById(bookId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(bookId)
        }

        exception.message shouldBe "Book not found with id: $bookId"
    }

    test("Given book with all fields When get book Then should return all fields") {
        // Given
        val bookId = UUID.randomUUID()
        val book = BookBuilder.aBook(
            id = bookId,
            isbn = "978-0-13-235088-4",
            title = "Clean Code",
            authors = listOf("Robert C. Martin"),
            categories = listOf("Programming", "Best Practices"),
            description = "A handbook",
            publishedYear = 2008
        )

        every { bookRepository.findById(bookId) } returns Optional.of(book)

        // When
        val response = handler.handle(bookId)

        // Then
        response.categories shouldBe listOf("Programming", "Best Practices")
        response.description shouldBe "A handbook"
        response.publishedYear shouldBe 2008
        response.createdAt shouldBe book.createdAt
    }

    test("Given book without optional fields When get book Then should return nulls") {
        // Given
        val bookId = UUID.randomUUID()
        val book = BookBuilder.aMinimalBook(id = bookId)

        every { bookRepository.findById(bookId) } returns Optional.of(book)

        // When
        val response = handler.handle(bookId)

        // Then
        response.authors shouldBe emptyList()
        response.categories shouldBe emptyList()
        response.description shouldBe null
        response.publishedYear shouldBe null
    }
})

