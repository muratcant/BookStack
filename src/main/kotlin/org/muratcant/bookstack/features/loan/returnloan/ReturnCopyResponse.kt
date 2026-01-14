package org.muratcant.bookstack.features.loan.returnloan

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class ReturnCopyResponse(
    @Schema(description = "Ödünç ID'si")
    val id: UUID,

    @Schema(description = "Üye ID'si")
    val memberId: UUID,

    @Schema(description = "Üye adı soyadı")
    val memberName: String,

    @Schema(description = "Kopya ID'si")
    val copyId: UUID,

    @Schema(description = "Kitap başlığı")
    val bookTitle: String,

    @Schema(description = "Kopya barkodu")
    val barcode: String,

    @Schema(description = "Ödünç alınma zamanı")
    val borrowedAt: LocalDateTime,

    @Schema(description = "İade tarihi")
    val dueDate: LocalDateTime,

    @Schema(description = "İade edilme zamanı")
    val returnedAt: LocalDateTime,

    @Schema(description = "Ödünç durumu")
    val status: String,

    @Schema(description = "Gecikme var mı?")
    val isOverdue: Boolean,

    @Schema(description = "Gecikme gün sayısı (varsa)")
    val daysOverdue: Int?
)
