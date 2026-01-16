package org.muratcant.bookstack.features.book.list

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.book.test.BookBuilder

class ListBooksHandlerTest : FunSpec({

    val bookRepository = mockk<BookRepository>()
    val handler = ListBooksHandler(bookRepository)

    beforeTest {
        clearMocks(bookRepository)
    }

    test("Given multiple books When list books Then should return all books") {
        // Given
        val book1 = BookBuilder.aBook(isbn = "978-0-13-235088-4", title = "Clean Code")
        val book2 = BookBuilder.aBook(isbn = "978-0-13-468599-1", title = "Clean Architecture")

        every { bookRepository.findAll() } returns listOf(book1, book2)

        // When
        val response = handler.handle()

        // Then
        response.books shouldHaveSize 2
        response.books[0].title shouldBe "Clean Code"
        response.books[1].title shouldBe "Clean Architecture"
    }

    test("Given no books When list books Then should return empty list") {
        // Given
        every { bookRepository.findAll() } returns emptyList()

        // When
        val response = handler.handle()

        // Then
        response.books.shouldBeEmpty()
    }

    test("Given single book When list books Then should return single item") {
        // Given
        val book = BookBuilder.aBook(
            isbn = "978-0-13-235088-4",
            title = "Clean Code",
            authors = listOf("Robert C. Martin"),
            publishedYear = 2008
        )

        every { bookRepository.findAll() } returns listOf(book)

        // When
        val response = handler.handle()

        // Then
        response.books shouldHaveSize 1
        response.books.first().isbn shouldBe "978-0-13-235088-4"
        response.books.first().title shouldBe "Clean Code"
        response.books.first().authors shouldBe listOf("Robert C. Martin")
        response.books.first().publishedYear shouldBe 2008
    }
})

