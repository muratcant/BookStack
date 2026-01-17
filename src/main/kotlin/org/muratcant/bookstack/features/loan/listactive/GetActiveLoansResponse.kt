package org.muratcant.bookstack.features.loan.listactive

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class GetActiveLoansResponse(
    @Schema(description = "List of active loans")
    val loans: List<ActiveLoanItem>
)

data class ActiveLoanItem(
    @Schema(description = "Loan ID")
    val id: UUID,

    @Schema(description = "Copy ID")
    val copyId: UUID,

    @Schema(description = "Book title")
    val bookTitle: String,

    @Schema(description = "Book ISBN")
    val bookIsbn: String,

    @Schema(description = "Copy barcode")
    val barcode: String,

    @Schema(description = "Borrowed at timestamp")
    val borrowedAt: LocalDateTime,

    @Schema(description = "Due date")
    val dueDate: LocalDateTime,

    @Schema(description = "Whether the loan is overdue")
    val isOverdue: Boolean
)
