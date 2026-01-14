package org.muratcant.bookstack.features.bookcopy.add

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.book.test.BookBuilder
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.domain.UsageType
import org.muratcant.bookstack.shared.exception.DuplicateResourceException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class AddBookCopyHandlerTest : FunSpec({

    val bookCopyRepository = mockk<BookCopyRepository>()
    val bookRepository = mockk<BookRepository>()
    val handler = AddBookCopyHandler(bookCopyRepository, bookRepository)

    beforeTest {
        clearMocks(bookCopyRepository, bookRepository)
    }

    test("Given valid request When add copy Then should save and return response") {
        // Given
        val bookId = UUID.randomUUID()
        val book = BookBuilder.aBook(id = bookId, title = "Clean Code")
        val request = AddBookCopyRequest(
            bookId = bookId,
            barcode = "BC-001-2024",
            usageType = UsageType.BOTH
        )

        every { bookRepository.findById(bookId) } returns Optional.of(book)
        every { bookCopyRepository.existsByBarcode(request.barcode) } returns false
        every { bookCopyRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(request)

        // Then
        response.id shouldNotBe null
        response.bookId shouldBe bookId
        response.bookTitle shouldBe "Clean Code"
        response.barcode shouldBe "BC-001-2024"
        response.usageType shouldBe "BOTH"
        response.status shouldBe "AVAILABLE"
        verify(exactly = 1) { bookCopyRepository.save(any()) }
    }

    test("Given non-existing book When add copy Then should throw ResourceNotFoundException") {
        // Given
        val bookId = UUID.randomUUID()
        val request = AddBookCopyRequest(
            bookId = bookId,
            barcode = "BC-001-2024",
            usageType = UsageType.BOTH
        )

        every { bookRepository.findById(bookId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(request)
        }

        exception.message shouldBe "Book not found with id: $bookId"
        verify(exactly = 0) { bookCopyRepository.save(any()) }
    }

    test("Given duplicate barcode When add copy Then should throw DuplicateResourceException") {
        // Given
        val bookId = UUID.randomUUID()
        val book = BookBuilder.aBook(id = bookId)
        val request = AddBookCopyRequest(
            bookId = bookId,
            barcode = "BC-EXISTING",
            usageType = UsageType.BOTH
        )

        every { bookRepository.findById(bookId) } returns Optional.of(book)
        every { bookCopyRepository.existsByBarcode(request.barcode) } returns true

        // When & Then
        val exception = shouldThrow<DuplicateResourceException> {
            handler.handle(request)
        }

        exception.message shouldBe "Barcode already exists: BC-EXISTING"
        verify(exactly = 0) { bookCopyRepository.save(any()) }
    }

    test("Given READING_ROOM_ONLY usage type When add copy Then should set correct usage type") {
        // Given
        val bookId = UUID.randomUUID()
        val book = BookBuilder.aBook(id = bookId)
        val request = AddBookCopyRequest(
            bookId = bookId,
            barcode = "BC-READING-001",
            usageType = UsageType.READING_ROOM_ONLY
        )

        every { bookRepository.findById(bookId) } returns Optional.of(book)
        every { bookCopyRepository.existsByBarcode(request.barcode) } returns false
        every { bookCopyRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(request)

        // Then
        response.usageType shouldBe "READING_ROOM_ONLY"
    }
})

