package org.muratcant.bookstack.features.penalty.pay

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class PayPenaltyResponse(
    @Schema(description = "Penalty ID")
    val id: UUID,

    @Schema(description = "Member ID")
    val memberId: UUID,

    @Schema(description = "Member full name")
    val memberName: String,

    @Schema(description = "Paid amount")
    val amount: BigDecimal,

    @Schema(description = "Penalty status")
    val status: String,

    @Schema(description = "Paid at timestamp")
    val paidAt: LocalDateTime
)
