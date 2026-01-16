package org.muratcant.bookstack.features.penalty.list

import org.muratcant.bookstack.features.penalty.domain.Penalty
import org.muratcant.bookstack.features.penalty.domain.PenaltyRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListPenaltiesHandler(
    private val penaltyRepository: PenaltyRepository
) {
    @Transactional(readOnly = true)
    fun handle(): ListPenaltiesResponse {
        val penalties = penaltyRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))

        return ListPenaltiesResponse(
            penalties = penalties.map { it.toListItem() }
        )
    }

    private fun Penalty.toListItem() = PenaltyListItem(
        id = id,
        memberId = member.id,
        memberName = "${member.firstName} ${member.lastName}",
        membershipNumber = member.membershipNumber,
        bookTitle = loan.bookCopy.book.title,
        barcode = loan.bookCopy.barcode,
        amount = amount,
        daysOverdue = daysOverdue,
        status = status.name,
        createdAt = createdAt
    )
}
