package org.muratcant.bookstack.features.visit.history

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class VisitHistoryResponse(
    @Schema(description = "List of visits")
    val visits: List<VisitHistoryItem>
)

data class VisitHistoryItem(
    @Schema(description = "Visit ID")
    val id: UUID,

    @Schema(description = "Check-in time")
    val checkInTime: LocalDateTime,

    @Schema(description = "Check-out time (null if still inside)")
    val checkOutTime: LocalDateTime?,

    @Schema(description = "Whether the visit is active")
    val isActive: Boolean
)
