package org.muratcant.bookstack.features.visit.get

import org.muratcant.bookstack.features.visit.domain.Visit
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetVisitHandler(
    private val visitRepository: VisitRepository
) {
    @Transactional(readOnly = true)
    fun handle(visitId: UUID): GetVisitResponse {
        val visit = visitRepository.findById(visitId)
            .orElseThrow { ResourceNotFoundException("Visit not found: $visitId") }

        return visit.toResponse()
    }

    private fun Visit.toResponse() = GetVisitResponse(
        id = id,
        memberId = member.id,
        memberName = "${member.firstName} ${member.lastName}",
        membershipNumber = member.membershipNumber,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        isActive = isActive()
    )
}
