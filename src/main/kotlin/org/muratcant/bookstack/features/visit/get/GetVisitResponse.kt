package org.muratcant.bookstack.features.visit.get

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class GetVisitResponse(
    @Schema(description = "Ziyaret ID'si")
    val id: UUID,

    @Schema(description = "Üye ID'si")
    val memberId: UUID,

    @Schema(description = "Üye adı soyadı")
    val memberName: String,

    @Schema(description = "Üye numarası")
    val membershipNumber: String,

    @Schema(description = "Check-in zamanı")
    val checkInTime: LocalDateTime,

    @Schema(description = "Check-out zamanı (null ise hala içeride)")
    val checkOutTime: LocalDateTime?,

    @Schema(description = "Ziyaret aktif mi?")
    val isActive: Boolean
)
