package org.muratcant.bookstack.features.loan.listactive

import org.muratcant.bookstack.features.loan.domain.Loan
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.muratcant.bookstack.features.loan.domain.LoanStatus
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetActiveLoansHandler(
    private val loanRepository: LoanRepository,
    private val memberRepository: MemberRepository
) {
    @Transactional(readOnly = true)
    fun handle(memberId: UUID): GetActiveLoansResponse {
        if (!memberRepository.existsById(memberId)) {
            throw ResourceNotFoundException("Member not found: $memberId")
        }

        val activeLoans = loanRepository.findByMemberIdAndStatusOrderByBorrowedAtDesc(
            memberId,
            LoanStatus.ACTIVE
        )

        return GetActiveLoansResponse(
            loans = activeLoans.map { it.toActiveItem() }
        )
    }

    private fun Loan.toActiveItem() = ActiveLoanItem(
        id = id,
        copyId = copy.id,
        bookTitle = copy.book.title,
        bookIsbn = copy.book.isbn,
        barcode = copy.barcode,
        borrowedAt = borrowedAt,
        dueDate = dueDate,
        isOverdue = isOverdue()
    )
}
