package org.muratcant.bookstack.features.book.search

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class SearchBooksResponse(
    @Schema(description = "Search query")
    val query: String,

    @Schema(description = "Number of results found")
    val count: Int,

    @Schema(description = "List of matching books")
    val books: List<SearchBookItem>
)

data class SearchBookItem(
    @Schema(description = "Book ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: UUID,

    @Schema(description = "Book ISBN", example = "978-3-16-148410-0")
    val isbn: String,

    @Schema(description = "Book title", example = "Clean Code")
    val title: String,

    @Schema(description = "List of authors", example = "[\"Robert C. Martin\"]")
    val authors: List<String>,

    @Schema(description = "Year of publication", example = "2008")
    val publishedYear: Int?
)

