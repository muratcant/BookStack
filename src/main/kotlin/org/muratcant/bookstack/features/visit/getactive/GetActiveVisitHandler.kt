package org.muratcant.bookstack.features.visit.getactive

import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.visit.domain.Visit
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetActiveVisitHandler(
    private val visitRepository: VisitRepository,
    private val memberRepository: MemberRepository
) {
    @Transactional(readOnly = true)
    fun handle(memberId: UUID): GetActiveVisitResponse? {
        if (!memberRepository.existsById(memberId)) {
            throw ResourceNotFoundException("Member not found: $memberId")
        }

        val visit = visitRepository.findByMemberIdAndCheckOutTimeIsNull(memberId)
            ?: return null

        return visit.toResponse()
    }

    private fun Visit.toResponse() = GetActiveVisitResponse(
        id = id,
        memberId = member.id,
        memberName = "${member.firstName} ${member.lastName}",
        checkInTime = checkInTime
    )
}
