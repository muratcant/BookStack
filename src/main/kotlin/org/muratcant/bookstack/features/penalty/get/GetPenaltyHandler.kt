package org.muratcant.bookstack.features.penalty.get

import org.muratcant.bookstack.features.penalty.domain.Penalty
import org.muratcant.bookstack.features.penalty.domain.PenaltyRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetPenaltyHandler(
    private val penaltyRepository: PenaltyRepository
) {
    @Transactional(readOnly = true)
    fun handle(penaltyId: UUID): GetPenaltyResponse {
        val penalty = penaltyRepository.findById(penaltyId)
            .orElseThrow { ResourceNotFoundException("Penalty not found: $penaltyId") }

        return penalty.toResponse()
    }

    private fun Penalty.toResponse() = GetPenaltyResponse(
        id = id,
        memberId = member.id,
        memberName = "${member.firstName} ${member.lastName}",
        loanId = loan.id,
        bookTitle = loan.bookCopy.book.title,
        barcode = loan.bookCopy.barcode,
        amount = amount,
        daysOverdue = daysOverdue,
        status = status.name,
        paidAt = paidAt,
        createdAt = createdAt
    )
}
