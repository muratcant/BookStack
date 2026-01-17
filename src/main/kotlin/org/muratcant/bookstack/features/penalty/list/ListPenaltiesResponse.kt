package org.muratcant.bookstack.features.penalty.list

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ListPenaltiesResponse(
    @Schema(description = "List of penalties")
    val penalties: List<PenaltyListItem>
)

data class PenaltyListItem(
    @Schema(description = "Penalty ID")
    val id: UUID,

    @Schema(description = "Member ID")
    val memberId: UUID,

    @Schema(description = "Member full name")
    val memberName: String,

    @Schema(description = "Membership number")
    val membershipNumber: String,

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

    @Schema(description = "Created at timestamp")
    val createdAt: LocalDateTime
)
