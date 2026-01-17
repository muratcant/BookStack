package org.muratcant.bookstack.features.loan.get

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class GetLoanResponse(
    @Schema(description = "Loan ID")
    val id: UUID,

    @Schema(description = "Member ID")
    val memberId: UUID,

    @Schema(description = "Member full name")
    val memberName: String,

    @Schema(description = "Membership number")
    val membershipNumber: String,

    @Schema(description = "Copy ID")
    val copyId: UUID,

    @Schema(description = "Book ID")
    val bookId: UUID,

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

    @Schema(description = "Returned at timestamp (null if not yet returned)")
    val returnedAt: LocalDateTime?,

    @Schema(description = "Loan status")
    val status: String,

    @Schema(description = "Whether the loan is overdue")
    val isOverdue: Boolean
)
