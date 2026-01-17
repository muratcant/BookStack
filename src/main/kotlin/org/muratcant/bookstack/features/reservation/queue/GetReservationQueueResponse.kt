package org.muratcant.bookstack.features.reservation.queue

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class GetReservationQueueResponse(
    @Schema(description = "Book ID")
    val bookId: UUID,

    @Schema(description = "Book title")
    val bookTitle: String,

    @Schema(description = "Total number of waiting reservations")
    val totalWaiting: Int,

    @Schema(description = "Queue list")
    val queue: List<ReservationQueueItem>
)

data class ReservationQueueItem(
    @Schema(description = "Reservation ID")
    val id: UUID,

    @Schema(description = "Member ID")
    val memberId: UUID,

    @Schema(description = "Member full name")
    val memberName: String,

    @Schema(description = "Membership number")
    val membershipNumber: String,

    @Schema(description = "Queue position")
    val queuePosition: Int,

    @Schema(description = "Reservation status")
    val status: String,

    @Schema(description = "Pickup expiration timestamp (if any)")
    val expiresAt: LocalDateTime?,

    @Schema(description = "Created at timestamp")
    val createdAt: LocalDateTime
)
