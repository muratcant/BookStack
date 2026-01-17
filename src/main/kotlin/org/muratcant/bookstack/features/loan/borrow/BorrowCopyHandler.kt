package org.muratcant.bookstack.features.loan.borrow

import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.bookcopy.domain.UsageType
import org.muratcant.bookstack.features.loan.config.LoanProperties
import org.muratcant.bookstack.features.loan.domain.Loan
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.muratcant.bookstack.features.loan.domain.LoanStatus
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.penalty.config.PenaltyProperties
import org.muratcant.bookstack.features.penalty.domain.PenaltyRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import org.muratcant.bookstack.features.reservation.process.ProcessReservationService
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.muratcant.bookstack.shared.exception.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class BorrowCopyHandler(
    private val loanRepository: LoanRepository,
    private val memberRepository: MemberRepository,
    private val bookCopyRepository: BookCopyRepository,
    private val visitRepository: VisitRepository,
    private val penaltyRepository: PenaltyRepository,
    private val reservationRepository: ReservationRepository,
    private val processReservationService: ProcessReservationService,
    private val loanProperties: LoanProperties,
    private val penaltyProperties: PenaltyProperties
) {
    @Transactional
    fun handle(request: BorrowCopyRequest): BorrowCopyResponse {
        val member = memberRepository.findById(request.memberId)
            .orElseThrow { ResourceNotFoundException("Member not found: ${request.memberId}") }

        val copy = bookCopyRepository.findById(request.copyId)
            .orElseThrow { ResourceNotFoundException("Book copy not found: ${request.copyId}") }

        if (!member.isActive()) {
            throw MemberNotActiveException("Member is not active: ${member.status}")
        }

        if (!visitRepository.existsByMemberIdAndCheckOutTimeIsNull(request.memberId)) {
            throw MemberNotCheckedInException("Member must be checked in to borrow a copy")
        }

        val unpaidAmount = penaltyRepository.sumUnpaidAmountByMemberId(request.memberId)
        if (unpaidAmount >= penaltyProperties.blockingThreshold) {
            throw UnpaidPenaltiesException("Member has unpaid penalties ($unpaidAmount) above blocking threshold (${penaltyProperties.blockingThreshold})")
        }

        if (copy.status == CopyStatus.ON_HOLD) {
            val reservation = reservationRepository.findByCopyIdAndStatus(
                request.copyId, ReservationStatus.READY_FOR_PICKUP
            )
            if (reservation == null || reservation.member.id != request.memberId) {
                throw CopyNotAvailableException("Copy is on hold for another member's reservation")
            }
        } else if (copy.status != CopyStatus.AVAILABLE) {
            throw CopyNotAvailableException("Copy is not available: ${copy.status}")
        }

        if (copy.usageType == UsageType.READING_ROOM_ONLY) {
            throw CopyNotBorrowableException("Copy is for reading room only")
        }

        val activeLoansCount = loanRepository.countByMemberIdAndStatus(request.memberId, LoanStatus.ACTIVE)
        if (activeLoansCount >= member.maxActiveLoans) {
            throw MaxLoansExceededException("Member has reached maximum active loans limit: ${member.maxActiveLoans}")
        }

        val dueDate = LocalDateTime.now().plusDays(loanProperties.defaultDurationDays.toLong())
        val loan = Loan(
            member = member,
            bookCopy = copy,
            dueDate = dueDate
        )

        copy.status = CopyStatus.LOANED

        val savedLoan = loanRepository.save(loan)
        bookCopyRepository.save(copy)

        processReservationService.fulfillReservation(request.copyId)

        return savedLoan.toResponse()
    }

    private fun Loan.toResponse() = BorrowCopyResponse(
        id = id,
        memberId = member.id,
        memberName = "${member.firstName} ${member.lastName}",
        copyId = bookCopy.id,
        bookTitle = bookCopy.book.title,
        barcode = bookCopy.barcode,
        borrowedAt = borrowedAt,
        dueDate = dueDate,
        status = status.name
    )
}
