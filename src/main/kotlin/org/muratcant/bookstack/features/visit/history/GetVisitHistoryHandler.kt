package org.muratcant.bookstack.features.visit.history

import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.visit.domain.Visit
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetVisitHistoryHandler(
    private val visitRepository: VisitRepository,
    private val memberRepository: MemberRepository
) {
    @Transactional(readOnly = true)
    fun handle(memberId: UUID): VisitHistoryResponse {
        if (!memberRepository.existsById(memberId)) {
            throw ResourceNotFoundException("Member not found: $memberId")
        }

        val visits = visitRepository.findByMemberIdOrderByCheckInTimeDesc(memberId)

        return VisitHistoryResponse(
            visits = visits.map { it.toHistoryItem() }
        )
    }

    private fun Visit.toHistoryItem() = VisitHistoryItem(
        id = id,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        isActive = isActive()
    )
}
