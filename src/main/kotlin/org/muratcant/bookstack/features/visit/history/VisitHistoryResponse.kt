package org.muratcant.bookstack.features.visit.history

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class VisitHistoryResponse(
    @Schema(description = "Ziyaret listesi")
    val visits: List<VisitHistoryItem>
)

data class VisitHistoryItem(
    @Schema(description = "Ziyaret ID'si")
    val id: UUID,

    @Schema(description = "Check-in zamanı")
    val checkInTime: LocalDateTime,

    @Schema(description = "Check-out zamanı (null ise hala içeride)")
    val checkOutTime: LocalDateTime?,

    @Schema(description = "Ziyaret aktif mi?")
    val isActive: Boolean
)
