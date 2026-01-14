package org.muratcant.bookstack.features.bookcopy.listbybook

import io.swagger.v3.oas.annotations.media.Schema
import org.muratcant.bookstack.features.bookcopy.list.BookCopyItem
import java.util.UUID

data class ListBookCopiesByBookResponse(
    @Schema(description = "Book ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val bookId: UUID,

    @Schema(description = "Book title", example = "Clean Code")
    val bookTitle: String,

    @Schema(description = "List of copies for this book")
    val copies: List<BookCopyItem>
)

