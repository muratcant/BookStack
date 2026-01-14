package org.muratcant.bookstack.features.loan.listactive

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class GetActiveLoansResponse(
    @Schema(description = "Aktif ödünç listesi")
    val loans: List<ActiveLoanItem>
)

data class ActiveLoanItem(
    @Schema(description = "Ödünç ID'si")
    val id: UUID,

    @Schema(description = "Kopya ID'si")
    val copyId: UUID,

    @Schema(description = "Kitap başlığı")
    val bookTitle: String,

    @Schema(description = "Kitap ISBN")
    val bookIsbn: String,

    @Schema(description = "Kopya barkodu")
    val barcode: String,

    @Schema(description = "Ödünç alınma zamanı")
    val borrowedAt: LocalDateTime,

    @Schema(description = "İade tarihi")
    val dueDate: LocalDateTime,

    @Schema(description = "Gecikmiş mi?")
    val isOverdue: Boolean
)
