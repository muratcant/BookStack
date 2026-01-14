package org.muratcant.bookstack.features.bookcopy.list

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class ListBookCopiesResponse(
    @Schema(description = "List of book copies")
    val copies: List<BookCopyItem>
)

data class BookCopyItem(
    @Schema(description = "Copy ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: UUID,

    @Schema(description = "Book ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val bookId: UUID,

    @Schema(description = "Book title", example = "Clean Code")
    val bookTitle: String,

    @Schema(description = "Unique barcode", example = "BC-001-2024")
    val barcode: String,

    @Schema(description = "Usage type", example = "BOTH")
    val usageType: String,

    @Schema(description = "Copy status", example = "AVAILABLE")
    val status: String
)

