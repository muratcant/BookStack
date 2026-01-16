package org.muratcant.bookstack.features.penalty.create

import org.muratcant.bookstack.features.loan.domain.Loan
import org.muratcant.bookstack.features.penalty.config.PenaltyProperties
import org.muratcant.bookstack.features.penalty.domain.Penalty
import org.muratcant.bookstack.features.penalty.domain.PenaltyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class CreatePenaltyService(
    private val penaltyRepository: PenaltyRepository,
    private val penaltyProperties: PenaltyProperties
) {
    @Transactional
    fun createIfOverdue(loan: Loan, daysOverdue: Int): Penalty? {
        if (daysOverdue <= 0) {
            return null
        }

        if (penaltyRepository.existsByLoanId(loan.id)) {
            return null
        }

        val amount = penaltyProperties.dailyFee.multiply(BigDecimal(daysOverdue))

        val penalty = Penalty(
            member = loan.member,
            loan = loan,
            amount = amount,
            daysOverdue = daysOverdue
        )

        return penaltyRepository.save(penalty)
    }
}
