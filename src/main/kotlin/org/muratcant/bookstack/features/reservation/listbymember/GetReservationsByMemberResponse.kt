package org.muratcant.bookstack.features.reservation.listbymember

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class GetReservationsByMemberResponse(
    @Schema(description = "List of reservations")
    val reservations: List<MemberReservationItem>
)

data class MemberReservationItem(
    @Schema(description = "Reservation ID")
    val id: UUID,

    @Schema(description = "Book ID")
    val bookId: UUID,

    @Schema(description = "Book title")
    val bookTitle: String,

    @Schema(description = "Book ISBN")
    val isbn: String,

    @Schema(description = "Queue position")
    val queuePosition: Int,

    @Schema(description = "Reservation status")
    val status: String,

    @Schema(description = "Pickup expiration timestamp (if any)")
    val expiresAt: LocalDateTime?,

    @Schema(description = "Created at timestamp")
    val createdAt: LocalDateTime
)
