package org.muratcant.bookstack.features.penalty.pay

import org.muratcant.bookstack.features.penalty.domain.Penalty
import org.muratcant.bookstack.features.penalty.domain.PenaltyRepository
import org.muratcant.bookstack.shared.exception.BusinessRuleException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PayPenaltyHandler(
    private val penaltyRepository: PenaltyRepository
) {
    @Transactional
    fun handle(penaltyId: UUID): PayPenaltyResponse {
        val penalty = penaltyRepository.findById(penaltyId)
            .orElseThrow { ResourceNotFoundException("Penalty not found: $penaltyId") }

        if (!penalty.isUnpaid()) {
            throw BusinessRuleException("Penalty is already ${penalty.status.name.lowercase()}")
        }

        penalty.pay()
        val savedPenalty = penaltyRepository.save(penalty)

        return savedPenalty.toResponse()
    }

    private fun Penalty.toResponse() = PayPenaltyResponse(
        id = id,
        memberId = member.id,
        memberName = "${member.firstName} ${member.lastName}",
        amount = amount,
        status = status.name,
        paidAt = paidAt!!
    )
}
