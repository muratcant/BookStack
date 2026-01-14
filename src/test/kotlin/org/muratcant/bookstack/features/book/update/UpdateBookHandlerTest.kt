package org.muratcant.bookstack.features.book.update

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.book.test.BookBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class UpdateBookHandlerTest : FunSpec({

    val bookRepository = mockk<BookRepository>()
    val handler = UpdateBookHandler(bookRepository)

    beforeTest {
        clearMocks(bookRepository)
    }

    test("Given existing book and valid request When update Then should update and return response") {
        // Given
        val bookId = UUID.randomUUID()
        val existingBook = BookBuilder.aBook(
            id = bookId,
            isbn = "978-0-13-235088-4",
            title = "Clean Code",
            authors = listOf("Robert C. Martin")
        )
        val request = UpdateBookRequest(
            title = "Clean Code: Updated Edition",
            authors = listOf("Robert C. Martin", "New Author"),
            categories = listOf("Programming", "Best Practices"),
            description = "Updated description",
            publishedYear = 2020
        )

        every { bookRepository.findById(bookId) } returns Optional.of(existingBook)
        every { bookRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(bookId, request)

        // Then
        response.id shouldBe bookId
        response.isbn shouldBe "978-0-13-235088-4" // ISBN should not change
        response.title shouldBe "Clean Code: Updated Edition"
        response.authors shouldBe listOf("Robert C. Martin", "New Author")
        response.categories shouldBe listOf("Programming", "Best Practices")
        response.description shouldBe "Updated description"
        response.publishedYear shouldBe 2020
        verify(exactly = 1) { bookRepository.save(any()) }
    }

    test("Given non-existing book When update Then should throw ResourceNotFoundException") {
        // Given
        val bookId = UUID.randomUUID()
        val request = UpdateBookRequest(
            title = "Updated Title",
            authors = emptyList(),
            categories = emptyList(),
            description = null,
            publishedYear = null
        )

        every { bookRepository.findById(bookId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(bookId, request)
        }

        exception.message shouldBe "Book not found with id: $bookId"
        verify(exactly = 0) { bookRepository.save(any()) }
    }

    test("Given update with empty authors When update Then should clear authors list") {
        // Given
        val bookId = UUID.randomUUID()
        val existingBook = BookBuilder.aBook(
            id = bookId,
            authors = listOf("Author 1", "Author 2")
        )
        val request = UpdateBookRequest(
            title = "Updated Title",
            authors = emptyList(),
            categories = emptyList(),
            description = null,
            publishedYear = null
        )

        every { bookRepository.findById(bookId) } returns Optional.of(existingBook)
        every { bookRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(bookId, request)

        // Then
        response.authors shouldBe emptyList()
    }

    test("Given update with null description When update Then should set description to null") {
        // Given
        val bookId = UUID.randomUUID()
        val existingBook = BookBuilder.aBook(
            id = bookId,
            description = "Old description"
        )
        val request = UpdateBookRequest(
            title = "Updated Title",
            authors = emptyList(),
            categories = emptyList(),
            description = null,
            publishedYear = null
        )

        every { bookRepository.findById(bookId) } returns Optional.of(existingBook)
        every { bookRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(bookId, request)

        // Then
        response.description shouldBe null
    }
})

