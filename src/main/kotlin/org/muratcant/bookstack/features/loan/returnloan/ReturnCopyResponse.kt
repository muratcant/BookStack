package org.muratcant.bookstack.features.loan.returnloan

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ReturnCopyResponse(
    @Schema(description = "Loan ID")
    val id: UUID,

    @Schema(description = "Member ID")
    val memberId: UUID,

    @Schema(description = "Member full name")
    val memberName: String,

    @Schema(description = "Copy ID")
    val copyId: UUID,

    @Schema(description = "Book title")
    val bookTitle: String,

    @Schema(description = "Copy barcode")
    val barcode: String,

    @Schema(description = "Borrowed at timestamp")
    val borrowedAt: LocalDateTime,

    @Schema(description = "Due date")
    val dueDate: LocalDateTime,

    @Schema(description = "Returned at timestamp")
    val returnedAt: LocalDateTime,

    @Schema(description = "Loan status")
    val status: String,

    @Schema(description = "Whether the return was overdue")
    val isOverdue: Boolean,

    @Schema(description = "Number of days overdue (if any)")
    val daysOverdue: Int?,

    @Schema(description = "Created penalty ID (if any)")
    val penaltyId: UUID? = null,

    @Schema(description = "Penalty amount (if any)")
    val penaltyAmount: BigDecimal? = null,

    @Schema(description = "Whether the copy was assigned to a reservation holder")
    val reservationAssigned: Boolean = false,

    @Schema(description = "Assigned reservation ID (if any)")
    val reservationId: UUID? = null
)
