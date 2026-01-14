package org.muratcant.bookstack.features.loan.returnloan

import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.loan.domain.Loan
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.muratcant.bookstack.features.loan.domain.LoanStatus
import org.muratcant.bookstack.shared.exception.BusinessRuleException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class ReturnCopyHandler(
    private val loanRepository: LoanRepository,
    private val bookCopyRepository: BookCopyRepository
) {
    @Transactional
    fun handle(loanId: UUID): ReturnCopyResponse {
        val loan = loanRepository.findById(loanId)
            .orElseThrow { ResourceNotFoundException("Loan not found: $loanId") }

        if (loan.status != LoanStatus.ACTIVE) {
            throw BusinessRuleException("Loan is not active: ${loan.status}")
        }

        // Return the copy
        loan.returnCopy()

        // Update copy status to AVAILABLE
        val copy = loan.copy
        copy.status = CopyStatus.AVAILABLE

        val savedLoan = loanRepository.save(loan)
        bookCopyRepository.save(copy)

        return savedLoan.toResponse()
    }

    private fun Loan.toResponse(): ReturnCopyResponse {
        val now = LocalDateTime.now()
        val isOverdue = dueDate.isBefore(returnedAt ?: now)
        val daysOverdue = if (isOverdue) {
            ChronoUnit.DAYS.between(dueDate, returnedAt ?: now).toInt()
        } else null

        return ReturnCopyResponse(
            id = id,
            memberId = member.id,
            memberName = "${member.firstName} ${member.lastName}",
            copyId = copy.id,
            bookTitle = copy.book.title,
            barcode = copy.barcode,
            borrowedAt = borrowedAt,
            dueDate = dueDate,
            returnedAt = returnedAt!!,
            status = status.name,
            isOverdue = isOverdue,
            daysOverdue = daysOverdue
        )
    }
}
