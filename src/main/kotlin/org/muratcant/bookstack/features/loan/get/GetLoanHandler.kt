package org.muratcant.bookstack.features.loan.get

import org.muratcant.bookstack.features.loan.domain.Loan
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetLoanHandler(
    private val loanRepository: LoanRepository
) {
    @Transactional(readOnly = true)
    fun handle(loanId: UUID): GetLoanResponse {
        val loan = loanRepository.findById(loanId)
            .orElseThrow { ResourceNotFoundException("Loan not found: $loanId") }

        return loan.toResponse()
    }

    private fun Loan.toResponse() = GetLoanResponse(
        id = id,
        memberId = member.id,
        memberName = "${member.firstName} ${member.lastName}",
        membershipNumber = member.membershipNumber,
        copyId = copy.id,
        bookId = copy.book.id,
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
