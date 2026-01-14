package org.muratcant.bookstack.features.book.search

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.book.test.BookBuilder

class SearchBooksHandlerTest : FunSpec({

    val bookRepository = mockk<BookRepository>()
    val handler = SearchBooksHandler(bookRepository)

    beforeTest {
        clearMocks(bookRepository)
    }

    test("Given matching query When search Then should return matching books") {
        // Given
        val book = BookBuilder.aBook(
            isbn = "978-0-13-235088-4",
            title = "Clean Code",
            authors = listOf("Robert C. Martin")
        )

        every { bookRepository.search("Clean") } returns listOf(book)

        // When
        val response = handler.handle("Clean")

        // Then
        response.query shouldBe "Clean"
        response.count shouldBe 1
        response.books shouldHaveSize 1
        response.books[0].title shouldBe "Clean Code"
    }

    test("Given non-matching query When search Then should return empty list") {
        // Given
        every { bookRepository.search("NonExistent") } returns emptyList()

        // When
        val response = handler.handle("NonExistent")

        // Then
        response.query shouldBe "NonExistent"
        response.count shouldBe 0
        response.books.shouldBeEmpty()
    }

    test("Given blank query When search Then should return empty without calling repository") {
        // When
        val response = handler.handle("   ")

        // Then
        response.query shouldBe "   "
        response.count shouldBe 0
        response.books.shouldBeEmpty()
        verify(exactly = 0) { bookRepository.search(any()) }
    }

    test("Given empty query When search Then should return empty without calling repository") {
        // When
        val response = handler.handle("")

        // Then
        response.count shouldBe 0
        response.books.shouldBeEmpty()
        verify(exactly = 0) { bookRepository.search(any()) }
    }

    test("Given ISBN query When search Then should find book by ISBN") {
        // Given
        val book = BookBuilder.aBook(isbn = "978-0-13-235088-4", title = "Clean Code")

        every { bookRepository.search("978-0-13") } returns listOf(book)

        // When
        val response = handler.handle("978-0-13")

        // Then
        response.count shouldBe 1
        response.books[0].isbn shouldBe "978-0-13-235088-4"
    }

    test("Given query matching multiple books When search Then should return all matches") {
        // Given
        val book1 = BookBuilder.aBook(isbn = "978-0-13-235088-4", title = "Clean Code")
        val book2 = BookBuilder.aBook(isbn = "978-0-13-468599-1", title = "Clean Architecture")

        every { bookRepository.search("Clean") } returns listOf(book1, book2)

        // When
        val response = handler.handle("Clean")

        // Then
        response.count shouldBe 2
        response.books shouldHaveSize 2
    }
})

