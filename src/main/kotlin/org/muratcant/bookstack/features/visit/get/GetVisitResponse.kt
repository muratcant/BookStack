package org.muratcant.bookstack.features.visit.get

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class GetVisitResponse(
    @Schema(description = "Visit ID")
    val id: UUID,

    @Schema(description = "Member ID")
    val memberId: UUID,

    @Schema(description = "Member full name")
    val memberName: String,

    @Schema(description = "Membership number")
    val membershipNumber: String,

    @Schema(description = "Check-in time")
    val checkInTime: LocalDateTime,

    @Schema(description = "Check-out time (null if still inside)")
    val checkOutTime: LocalDateTime?,

    @Schema(description = "Whether the visit is active")
    val isActive: Boolean
)
