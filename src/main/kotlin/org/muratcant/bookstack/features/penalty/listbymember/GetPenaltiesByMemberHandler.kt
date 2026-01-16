package org.muratcant.bookstack.features.penalty.listbymember

import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.penalty.domain.Penalty
import org.muratcant.bookstack.features.penalty.domain.PenaltyRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetPenaltiesByMemberHandler(
    private val penaltyRepository: PenaltyRepository,
    private val memberRepository: MemberRepository
) {
    @Transactional(readOnly = true)
    fun handle(memberId: UUID): GetPenaltiesByMemberResponse {
        if (!memberRepository.existsById(memberId)) {
            throw ResourceNotFoundException("Member not found: $memberId")
        }

        val penalties = penaltyRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
        val totalUnpaid = penaltyRepository.sumUnpaidAmountByMemberId(memberId)

        return GetPenaltiesByMemberResponse(
            totalUnpaidAmount = totalUnpaid,
            penalties = penalties.map { it.toItem() }
        )
    }

    private fun Penalty.toItem() = PenaltyItem(
        id = id,
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
