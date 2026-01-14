package org.muratcant.bookstack.features.bookcopy.update

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.bookcopy.domain.UsageType
import org.muratcant.bookstack.features.bookcopy.test.BookCopyBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class UpdateBookCopyHandlerTest : FunSpec({

    val bookCopyRepository = mockk<BookCopyRepository>()
    val handler = UpdateBookCopyHandler(bookCopyRepository)

    beforeTest {
        clearMocks(bookCopyRepository)
    }

    test("Given existing copy When update Then should update and return response") {
        // Given
        val copyId = UUID.randomUUID()
        val existingCopy = BookCopyBuilder.aBookCopy(
            id = copyId,
            usageType = UsageType.BOTH,
            status = CopyStatus.AVAILABLE
        )
        val request = UpdateBookCopyRequest(
            usageType = UsageType.BORROWABLE,
            status = CopyStatus.DAMAGED
        )

        every { bookCopyRepository.findById(copyId) } returns Optional.of(existingCopy)
        every { bookCopyRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(copyId, request)

        // Then
        response.id shouldBe copyId
        response.usageType shouldBe "BORROWABLE"
        response.status shouldBe "DAMAGED"
        verify(exactly = 1) { bookCopyRepository.save(any()) }
    }

    test("Given non-existing copy When update Then should throw ResourceNotFoundException") {
        // Given
        val copyId = UUID.randomUUID()
        val request = UpdateBookCopyRequest(
            usageType = UsageType.BOTH,
            status = CopyStatus.AVAILABLE
        )

        every { bookCopyRepository.findById(copyId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(copyId, request)
        }

        exception.message shouldBe "Book copy not found with id: $copyId"
        verify(exactly = 0) { bookCopyRepository.save(any()) }
    }

    test("Given copy When update status to LOANED Then should update correctly") {
        // Given
        val copyId = UUID.randomUUID()
        val existingCopy = BookCopyBuilder.anAvailableCopy(id = copyId)
        val request = UpdateBookCopyRequest(
            usageType = UsageType.BOTH,
            status = CopyStatus.LOANED
        )

        every { bookCopyRepository.findById(copyId) } returns Optional.of(existingCopy)
        every { bookCopyRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(copyId, request)

        // Then
        response.status shouldBe "LOANED"
    }

    test("Given copy When update usage type to READING_ROOM_ONLY Then should update correctly") {
        // Given
        val copyId = UUID.randomUUID()
        val existingCopy = BookCopyBuilder.aBorrowableCopy(id = copyId)
        val request = UpdateBookCopyRequest(
            usageType = UsageType.READING_ROOM_ONLY,
            status = CopyStatus.AVAILABLE
        )

        every { bookCopyRepository.findById(copyId) } returns Optional.of(existingCopy)
        every { bookCopyRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(copyId, request)

        // Then
        response.usageType shouldBe "READING_ROOM_ONLY"
    }
})

