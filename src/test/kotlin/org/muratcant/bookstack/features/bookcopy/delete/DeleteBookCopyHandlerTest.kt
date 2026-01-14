package org.muratcant.bookstack.features.bookcopy.delete

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.UUID

class DeleteBookCopyHandlerTest : FunSpec({

    val bookCopyRepository = mockk<BookCopyRepository>()
    val handler = DeleteBookCopyHandler(bookCopyRepository)

    beforeTest {
        clearMocks(bookCopyRepository)
    }

    test("Given existing copy When delete Then should delete successfully") {
        // Given
        val copyId = UUID.randomUUID()

        every { bookCopyRepository.existsById(copyId) } returns true
        justRun { bookCopyRepository.deleteById(copyId) }

        // When
        handler.handle(copyId)

        // Then
        verify(exactly = 1) { bookCopyRepository.deleteById(copyId) }
    }

    test("Given non-existing copy When delete Then should throw ResourceNotFoundException") {
        // Given
        val copyId = UUID.randomUUID()

        every { bookCopyRepository.existsById(copyId) } returns false

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(copyId)
        }

        exception.message shouldBe "Book copy not found with id: $copyId"
        verify(exactly = 0) { bookCopyRepository.deleteById(any()) }
    }
})

