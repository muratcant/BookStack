package org.muratcant.bookstack.features.book.add

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AddBookRequest(
    @field:NotBlank(message = "ISBN is required")
    @field:Size(min = 10, max = 17, message = "ISBN must be between 10 and 17 characters")
    @Schema(description = "Book ISBN (10 or 13 digits)", example = "978-3-16-148410-0")
    val isbn: String,

    @field:NotBlank(message = "Title is required")
    @Schema(description = "Book title", example = "Clean Code")
    val title: String,

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Schema(description = "List of authors", example = "[\"Robert C. Martin\"]")
    val authors: List<String> = emptyList(),

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Schema(description = "List of categories", example = "[\"Programming\", \"Software Engineering\"]")
    val categories: List<String> = emptyList(),

    @Schema(description = "Book description", example = "A handbook of agile software craftsmanship")
    val description: String? = null,

    @Schema(description = "Year of publication", example = "2008")
    val publishedYear: Int? = null
)

