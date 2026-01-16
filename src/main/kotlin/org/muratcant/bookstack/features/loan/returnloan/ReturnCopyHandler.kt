package org.muratcant.bookstack.features.loan.returnloan

import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.loan.domain.Loan
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.muratcant.bookstack.features.penalty.create.CreatePenaltyService
import org.muratcant.bookstack.features.penalty.domain.Penalty
import org.muratcant.bookstack.features.reservation.domain.Reservation
import org.muratcant.bookstack.features.reservation.process.ProcessReservationService
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
    private val bookCopyRepository: BookCopyRepository,
    private val createPenaltyService: CreatePenaltyService,
    private val processReservationService: ProcessReservationService
) {
    @Transactional
    fun handle(loanId: UUID): ReturnCopyResponse {
        val loan = loanRepository.findById(loanId)
            .orElseThrow { ResourceNotFoundException("Loan not found: $loanId") }

        if (!loan.isActive()) {
            throw BusinessRuleException("Loan is not active: ${loan.status}")
        }

        loan.returnCopy()

        val copy = loan.bookCopy
        copy.setAvailable()

        val savedLoan = loanRepository.save(loan)
        bookCopyRepository.save(copy)

        val daysOverdue = calculateDaysOverdue(savedLoan)
        val penalty = if (daysOverdue > 0) {
            createPenaltyService.createIfOverdue(savedLoan, daysOverdue)
        } else null

        val processedReservation = processReservationService.processAfterReturn(copy)

        return savedLoan.toResponse(penalty, processedReservation)
    }

    private fun calculateDaysOverdue(loan: Loan): Int {
        val returnedAt = loan.returnedAt ?: LocalDateTime.now()
        return if (loan.dueDate.isBefore(returnedAt)) {
            ChronoUnit.DAYS.between(loan.dueDate, returnedAt).toInt()
        } else 0
    }

    private fun Loan.toResponse(penalty: Penalty?, reservation: Reservation?): ReturnCopyResponse {
        val isOverdue = dueDate.isBefore(returnedAt!!)
        val daysOverdueVal = if (isOverdue) {
            ChronoUnit.DAYS.between(dueDate, returnedAt!!).toInt()
        } else null

        return ReturnCopyResponse(
            id = id,
            memberId = member.id,
            memberName = "${member.firstName} ${member.lastName}",
            copyId = bookCopy.id,
            bookTitle = bookCopy.book.title,
            barcode = bookCopy.barcode,
            borrowedAt = borrowedAt,
            dueDate = dueDate,
            returnedAt = returnedAt!!,
            status = status.name,
            isOverdue = isOverdue,
            daysOverdue = daysOverdueVal,
            penaltyId = penalty?.id,
            penaltyAmount = penalty?.amount,
            reservationAssigned = reservation != null,
            reservationId = reservation?.id
        )
    }
}
