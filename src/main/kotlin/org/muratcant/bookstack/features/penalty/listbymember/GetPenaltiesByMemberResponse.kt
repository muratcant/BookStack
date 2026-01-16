package org.muratcant.bookstack.features.penalty.listbymember

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class GetPenaltiesByMemberResponse(
    @Schema(description = "Toplam ödenmemiş ceza tutarı")
    val totalUnpaidAmount: BigDecimal,

    @Schema(description = "Ceza listesi")
    val penalties: List<PenaltyItem>
)

data class PenaltyItem(
    @Schema(description = "Ceza ID'si")
    val id: UUID,

    @Schema(description = "Ödünç ID'si")
    val loanId: UUID,

    @Schema(description = "Kitap başlığı")
    val bookTitle: String,

    @Schema(description = "Kopya barkodu")
    val barcode: String,

    @Schema(description = "Ceza tutarı")
    val amount: BigDecimal,

    @Schema(description = "Gecikme gün sayısı")
    val daysOverdue: Int,

    @Schema(description = "Ceza durumu")
    val status: String,

    @Schema(description = "Ödeme zamanı")
    val paidAt: LocalDateTime?,

    @Schema(description = "Oluşturulma zamanı")
    val createdAt: LocalDateTime
)
