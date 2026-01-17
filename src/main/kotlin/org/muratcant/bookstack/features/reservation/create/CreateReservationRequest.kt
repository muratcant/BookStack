package org.muratcant.bookstack.features.reservation.create

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class CreateReservationRequest(
    @field:NotNull(message = "Member ID is required")
    @Schema(description = "ID of the member making the reservation")
    val memberId: UUID,

    @field:NotNull(message = "Book ID is required")
    @Schema(description = "ID of the book to reserve")
    val bookId: UUID
)
