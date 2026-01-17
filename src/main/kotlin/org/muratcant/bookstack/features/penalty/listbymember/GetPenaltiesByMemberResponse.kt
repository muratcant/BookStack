package org.muratcant.bookstack.features.penalty.listbymember

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class GetPenaltiesByMemberResponse(
    @Schema(description = "Total unpaid penalty amount")
    val totalUnpaidAmount: BigDecimal,

    @Schema(description = "List of penalties")
    val penalties: List<PenaltyItem>
)

data class PenaltyItem(
    @Schema(description = "Penalty ID")
    val id: UUID,

    @Schema(description = "Loan ID")
    val loanId: UUID,

    @Schema(description = "Book title")
    val bookTitle: String,

    @Schema(description = "Copy barcode")
    val barcode: String,

    @Schema(description = "Penalty amount")
    val amount: BigDecimal,

    @Schema(description = "Number of days overdue")
    val daysOverdue: Int,

    @Schema(description = "Penalty status")
    val status: String,

    @Schema(description = "Paid at timestamp")
    val paidAt: LocalDateTime?,

    @Schema(description = "Created at timestamp")
    val createdAt: LocalDateTime
)
