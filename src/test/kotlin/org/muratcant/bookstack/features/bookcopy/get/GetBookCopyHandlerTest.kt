package org.muratcant.bookstack.features.bookcopy.get

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.book.test.BookBuilder
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.bookcopy.domain.UsageType
import org.muratcant.bookstack.features.bookcopy.test.BookCopyBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class GetBookCopyHandlerTest : FunSpec({

    val bookCopyRepository = mockk<BookCopyRepository>()
    val handler = GetBookCopyHandler(bookCopyRepository)

    beforeTest {
        clearMocks(bookCopyRepository)
    }

    test("Given existing copy id When get copy Then should return response") {
        // Given
        val copyId = UUID.randomUUID()
        val book = BookBuilder.aBook(isbn = "978-0-13-235088-4", title = "Clean Code")
        val copy = BookCopyBuilder.aBookCopy(
            id = copyId,
            book = book,
            barcode = "BC-001",
            usageType = UsageType.BOTH,
            status = CopyStatus.AVAILABLE
        )

        every { bookCopyRepository.findById(copyId) } returns Optional.of(copy)

        // When
        val response = handler.handle(copyId)

        // Then
        response.id shouldBe copyId
        response.bookId shouldBe book.id
        response.bookTitle shouldBe "Clean Code"
        response.bookIsbn shouldBe "978-0-13-235088-4"
        response.barcode shouldBe "BC-001"
        response.usageType shouldBe "BOTH"
        response.status shouldBe "AVAILABLE"
    }

    test("Given non-existing copy id When get copy Then should throw ResourceNotFoundException") {
        // Given
        val copyId = UUID.randomUUID()

        every { bookCopyRepository.findById(copyId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(copyId)
        }

        exception.message shouldBe "Book copy not found with id: $copyId"
    }

    test("Given loaned copy When get copy Then should return LOANED status") {
        // Given
        val copyId = UUID.randomUUID()
        val copy = BookCopyBuilder.aLoanedCopy(id = copyId)

        every { bookCopyRepository.findById(copyId) } returns Optional.of(copy)

        // When
        val response = handler.handle(copyId)

        // Then
        response.status shouldBe "LOANED"
    }
})

