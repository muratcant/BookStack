package org.muratcant.bookstack.features.visit.checkout

import org.muratcant.bookstack.features.visit.domain.Visit
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.muratcant.bookstack.shared.exception.BusinessRuleException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CheckOutHandler(
    private val visitRepository: VisitRepository
) {
    @Transactional
    fun handle(visitId: UUID): CheckOutResponse {
        val visit = visitRepository.findById(visitId)
            .orElseThrow { ResourceNotFoundException("Visit not found: $visitId") }

        if (!visit.isActive()) {
            throw BusinessRuleException("Visit is already checked out")
        }

        visit.checkOut()
        val savedVisit = visitRepository.save(visit)

        return savedVisit.toResponse()
    }

    private fun Visit.toResponse() = CheckOutResponse(
        id = id,
        memberId = member.id,
        memberName = "${member.firstName} ${member.lastName}",
        checkInTime = checkInTime,
        checkOutTime = checkOutTime!!
    )
}
