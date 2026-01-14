package org.muratcant.bookstack.features.visit.checkin

import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.domain.MemberStatus
import org.muratcant.bookstack.features.visit.domain.Visit
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.muratcant.bookstack.shared.exception.MemberAlreadyCheckedInException
import org.muratcant.bookstack.shared.exception.MemberNotActiveException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CheckInHandler(
    private val visitRepository: VisitRepository,
    private val memberRepository: MemberRepository
) {
    @Transactional
    fun handle(request: CheckInRequest): CheckInResponse {
        val member = memberRepository.findById(request.memberId)
            .orElseThrow { ResourceNotFoundException("Member not found: ${request.memberId}") }

        if (member.status != MemberStatus.ACTIVE) {
            throw MemberNotActiveException("Member is not active: ${member.status}")
        }

        if (visitRepository.existsByMemberIdAndCheckOutTimeIsNull(request.memberId)) {
            throw MemberAlreadyCheckedInException("Member is already checked in")
        }

        val visit = Visit(member = member)
        val savedVisit = visitRepository.save(visit)

        return savedVisit.toResponse()
    }

    private fun Visit.toResponse() = CheckInResponse(
        id = id,
        memberId = member.id,
        memberName = "${member.firstName} ${member.lastName}",
        checkInTime = checkInTime
    )
}
