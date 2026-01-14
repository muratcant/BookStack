package org.muratcant.bookstack.features.book.test

import org.muratcant.bookstack.features.book.domain.Book
import java.time.LocalDateTime
import java.util.UUID

object BookBuilder {
    fun aBook(
        id: UUID = UUID.randomUUID(),
        isbn: String = "978-0-13-235088-4",
        title: String = "Clean Code",
        authors: List<String> = listOf("Robert C. Martin"),
        categories: List<String> = listOf("Programming", "Software Engineering"),
        description: String? = "A Handbook of Agile Software Craftsmanship",
        publishedYear: Int? = 2008,
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now()
    ): Book = Book(
        id = id,
        isbn = isbn,
        title = title,
        authors = authors,
        categories = categories,
        description = description,
        publishedYear = publishedYear,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun aBookWithoutDescription(
        id: UUID = UUID.randomUUID(),
        isbn: String = "978-0-13-235088-4",
        title: String = "Clean Code",
        authors: List<String> = listOf("Robert C. Martin"),
        categories: List<String> = listOf("Programming")
    ) = aBook(
        id = id,
        isbn = isbn,
        title = title,
        authors = authors,
        categories = categories,
        description = null,
        publishedYear = null
    )

    fun aMinimalBook(
        id: UUID = UUID.randomUUID(),
        isbn: String = "978-0-13-235088-4",
        title: String = "Clean Code"
    ) = aBook(
        id = id,
        isbn = isbn,
        title = title,
        authors = emptyList(),
        categories = emptyList(),
        description = null,
        publishedYear = null
    )
}

