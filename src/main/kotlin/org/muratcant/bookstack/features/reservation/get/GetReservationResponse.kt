package org.muratcant.bookstack.features.reservation.get

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class GetReservationResponse(
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

    @Schema(description = "Assigned copy ID (if any)")
    val copyId: UUID?,

    @Schema(description = "Assigned copy barcode (if any)")
    val barcode: String?,

    @Schema(description = "Queue position")
    val queuePosition: Int,

    @Schema(description = "Reservation status")
    val status: String,

    @Schema(description = "Notified at timestamp")
    val notifiedAt: LocalDateTime?,

    @Schema(description = "Pickup expiration timestamp")
    val expiresAt: LocalDateTime?,

    @Schema(description = "Created at timestamp")
    val createdAt: LocalDateTime
)
