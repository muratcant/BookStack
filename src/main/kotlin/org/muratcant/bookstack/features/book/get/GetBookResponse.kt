package org.muratcant.bookstack.features.book.get

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class GetBookResponse(
    @Schema(description = "Book ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: UUID,

    @Schema(description = "Book ISBN", example = "978-3-16-148410-0")
    val isbn: String,

    @Schema(description = "Book title", example = "Clean Code")
    val title: String,

    @Schema(description = "List of authors", example = "[\"Robert C. Martin\"]")
    val authors: List<String>,

    @Schema(description = "List of categories", example = "[\"Programming\"]")
    val categories: List<String>,

    @Schema(description = "Book description")
    val description: String?,

    @Schema(description = "Year of publication", example = "2008")
    val publishedYear: Int?,

    @Schema(description = "Creation timestamp")
    val createdAt: LocalDateTime
)

