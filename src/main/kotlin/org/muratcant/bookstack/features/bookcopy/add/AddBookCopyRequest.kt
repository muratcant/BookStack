package org.muratcant.bookstack.features.bookcopy.add

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.muratcant.bookstack.features.bookcopy.domain.UsageType
import java.util.UUID

data class AddBookCopyRequest(
    @field:NotNull(message = "Book ID is required")
    @Schema(description = "ID of the book this copy belongs to", example = "123e4567-e89b-12d3-a456-426614174000")
    var bookId: UUID,

    @field:NotBlank(message = "Barcode is required")
    @Schema(description = "Unique barcode for this copy", example = "BC-001-2024")
    val barcode: String,

    @Schema(description = "Usage type of the copy", example = "BOTH")
    val usageType: UsageType = UsageType.BOTH
)

