package org.muratcant.bookstack.features.bookcopy.listbybook

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.book.test.BookBuilder
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.test.BookCopyBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class ListBookCopiesByBookHandlerTest : FunSpec({

    val bookCopyRepository = mockk<BookCopyRepository>()
    val bookRepository = mockk<BookRepository>()
    val handler = ListBookCopiesByBookHandler(bookCopyRepository, bookRepository)

    beforeTest {
        clearMocks(bookCopyRepository, bookRepository)
    }

    test("Given book with copies When list by book Then should return copies") {
        // Given
        val bookId = UUID.randomUUID()
        val book = BookBuilder.aBook(id = bookId, title = "Clean Code")
        val copy1 = BookCopyBuilder.aBookCopy(book = book, barcode = "BC-001")
        val copy2 = BookCopyBuilder.aBookCopy(book = book, barcode = "BC-002")

        every { bookRepository.findById(bookId) } returns Optional.of(book)
        every { bookCopyRepository.findByBookId(bookId) } returns listOf(copy1, copy2)

        // When
        val response = handler.handle(bookId)

        // Then
        response.bookId shouldBe bookId
        response.bookTitle shouldBe "Clean Code"
        response.copies shouldHaveSize 2
    }

    test("Given book without copies When list by book Then should return empty list") {
        // Given
        val bookId = UUID.randomUUID()
        val book = BookBuilder.aBook(id = bookId, title = "New Book")

        every { bookRepository.findById(bookId) } returns Optional.of(book)
        every { bookCopyRepository.findByBookId(bookId) } returns emptyList()

        // When
        val response = handler.handle(bookId)

        // Then
        response.bookId shouldBe bookId
        response.copies.shouldBeEmpty()
    }

    test("Given non-existing book When list by book Then should throw ResourceNotFoundException") {
        // Given
        val bookId = UUID.randomUUID()

        every { bookRepository.findById(bookId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(bookId)
        }

        exception.message shouldBe "Book not found with id: $bookId"
    }
})

