package org.muratcant.bookstack.features.loan.borrow

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class BorrowCopyRequest(
    @field:NotNull(message = "Member ID is required")
    @Schema(description = "ID of the member borrowing the copy", example = "123e4567-e89b-12d3-a456-426614174000")
    val memberId: UUID,

    @field:NotNull(message = "Copy ID is required")
    @Schema(description = "ID of the copy to borrow", example = "123e4567-e89b-12d3-a456-426614174000")
    val copyId: UUID
)
