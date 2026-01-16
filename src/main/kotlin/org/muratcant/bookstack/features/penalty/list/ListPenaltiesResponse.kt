package org.muratcant.bookstack.features.penalty.list

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ListPenaltiesResponse(
    @Schema(description = "Ceza listesi")
    val penalties: List<PenaltyListItem>
)

data class PenaltyListItem(
    @Schema(description = "Ceza ID'si")
    val id: UUID,

    @Schema(description = "Üye ID'si")
    val memberId: UUID,

    @Schema(description = "Üye adı soyadı")
    val memberName: String,

    @Schema(description = "Üyelik numarası")
    val membershipNumber: String,

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

    @Schema(description = "Oluşturulma zamanı")
    val createdAt: LocalDateTime
)
