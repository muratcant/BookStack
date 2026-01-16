package org.muratcant.bookstack.features.penalty.pay

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class PayPenaltyResponse(
    @Schema(description = "Ceza ID'si")
    val id: UUID,

    @Schema(description = "Üye ID'si")
    val memberId: UUID,

    @Schema(description = "Üye adı soyadı")
    val memberName: String,

    @Schema(description = "Ödenen tutar")
    val amount: BigDecimal,

    @Schema(description = "Ceza durumu")
    val status: String,

    @Schema(description = "Ödeme zamanı")
    val paidAt: LocalDateTime
)
