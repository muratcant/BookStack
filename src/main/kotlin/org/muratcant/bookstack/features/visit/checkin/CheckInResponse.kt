package org.muratcant.bookstack.features.visit.checkin

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class CheckInResponse(
    @Schema(description = "Visit ID")
    val id: UUID,

    @Schema(description = "Member ID")
    val memberId: UUID,

    @Schema(description = "Member full name")
    val memberName: String,

    @Schema(description = "Check-in time")
    val checkInTime: LocalDateTime
)
