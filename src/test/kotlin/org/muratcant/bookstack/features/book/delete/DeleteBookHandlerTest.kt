package org.muratcant.bookstack.features.book.delete

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.UUID

class DeleteBookHandlerTest : FunSpec({

    val bookRepository = mockk<BookRepository>()
    val handler = DeleteBookHandler(bookRepository)

    beforeTest {
        clearMocks(bookRepository)
    }

    test("Given existing book When delete Then should delete successfully") {
        // Given
        val bookId = UUID.randomUUID()

        every { bookRepository.existsById(bookId) } returns true
        justRun { bookRepository.deleteById(bookId) }

        // When
        handler.handle(bookId)

        // Then
        verify(exactly = 1) { bookRepository.deleteById(bookId) }
    }

    test("Given non-existing book When delete Then should throw ResourceNotFoundException") {
        // Given
        val bookId = UUID.randomUUID()

        every { bookRepository.existsById(bookId) } returns false

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(bookId)
        }

        exception.message shouldBe "Book not found with id: $bookId"
        verify(exactly = 0) { bookRepository.deleteById(any()) }
    }
})

