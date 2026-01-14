package org.muratcant.bookstack.features.visit.getactive

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class GetActiveVisitResponse(
    @Schema(description = "Ziyaret ID'si")
    val id: UUID,

    @Schema(description = "Üye ID'si")
    val memberId: UUID,

    @Schema(description = "Üye adı soyadı")
    val memberName: String,

    @Schema(description = "Check-in zamanı")
    val checkInTime: LocalDateTime
)
