package org.muratcant.bookstack.features.reservation.create

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class CreateReservationResponse(
    @Schema(description = "Reservation ID")
    val id: UUID,

    @Schema(description = "Member ID")
    val memberId: UUID,

    @Schema(description = "Member full name")
    val memberName: String,

    @Schema(description = "Book ID")
    val bookId: UUID,

    @Schema(description = "Book title")
    val bookTitle: String,

    @Schema(description = "Queue position")
    val queuePosition: Int,

    @Schema(description = "Reservation status")
    val status: String,

    @Schema(description = "Created at timestamp")
    val createdAt: LocalDateTime
)
