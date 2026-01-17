package org.muratcant.bookstack.features.book.update

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class UpdateBookRequest(
    @field:NotBlank(message = "Title is required")
    @Schema(description = "Book title", example = "Clean Code: A Handbook")
    val title: String,

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Schema(description = "List of authors", example = "[\"Robert C. Martin\"]")
    val authors: List<String> = emptyList(),

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Schema(description = "List of categories", example = "[\"Programming\", \"Best Practices\"]")
    val categories: List<String> = emptyList(),

    @Schema(description = "Book description", example = "Updated description")
    val description: String? = null,

    @Schema(description = "Year of publication", example = "2008")
    val publishedYear: Int? = null
)

