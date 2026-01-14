package org.muratcant.bookstack.features.loan.history

import org.muratcant.bookstack.features.loan.domain.Loan
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetLoanHistoryHandler(
    private val loanRepository: LoanRepository,
    private val memberRepository: MemberRepository
) {
    @Transactional(readOnly = true)
    fun handle(memberId: UUID): GetLoanHistoryResponse {
        if (!memberRepository.existsById(memberId)) {
            throw ResourceNotFoundException("Member not found: $memberId")
        }

        val loans = loanRepository.findByMemberIdOrderByBorrowedAtDesc(memberId)

        return GetLoanHistoryResponse(
            loans = loans.map { it.toHistoryItem() }
        )
    }

    private fun Loan.toHistoryItem() = LoanHistoryItem(
        id = id,
        copyId = copy.id,
        bookTitle = copy.book.title,
        bookIsbn = copy.book.isbn,
        barcode = copy.barcode,
        borrowedAt = borrowedAt,
        dueDate = dueDate,
        returnedAt = returnedAt,
        status = status.name,
        isOverdue = isOverdue()
    )
}
