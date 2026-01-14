package org.muratcant.bookstack.features.bookcopy.list

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.book.test.BookBuilder
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.test.BookCopyBuilder

class ListBookCopiesHandlerTest : FunSpec({

    val bookCopyRepository = mockk<BookCopyRepository>()
    val handler = ListBookCopiesHandler(bookCopyRepository)

    beforeTest {
        clearMocks(bookCopyRepository)
    }

    test("Given multiple copies When list copies Then should return all") {
        // Given
        val book1 = BookBuilder.aBook(title = "Clean Code")
        val book2 = BookBuilder.aBook(title = "Clean Architecture")
        val copy1 = BookCopyBuilder.aBookCopy(book = book1, barcode = "BC-001")
        val copy2 = BookCopyBuilder.aBookCopy(book = book2, barcode = "BC-002")

        every { bookCopyRepository.findAll() } returns listOf(copy1, copy2)

        // When
        val response = handler.handle()

        // Then
        response.copies shouldHaveSize 2
        response.copies[0].bookTitle shouldBe "Clean Code"
        response.copies[1].bookTitle shouldBe "Clean Architecture"
    }

    test("Given no copies When list copies Then should return empty list") {
        // Given
        every { bookCopyRepository.findAll() } returns emptyList()

        // When
        val response = handler.handle()

        // Then
        response.copies.shouldBeEmpty()
    }

    test("Given single copy When list copies Then should return single item") {
        // Given
        val book = BookBuilder.aBook(title = "Clean Code")
        val copy = BookCopyBuilder.aBookCopy(book = book, barcode = "BC-001")

        every { bookCopyRepository.findAll() } returns listOf(copy)

        // When
        val response = handler.handle()

        // Then
        response.copies shouldHaveSize 1
        response.copies[0].barcode shouldBe "BC-001"
    }
})

