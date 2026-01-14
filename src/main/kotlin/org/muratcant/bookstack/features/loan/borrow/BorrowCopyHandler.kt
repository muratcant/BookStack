package org.muratcant.bookstack.features.loan.borrow

import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.bookcopy.domain.UsageType
import org.muratcant.bookstack.features.loan.config.LoanProperties
import org.muratcant.bookstack.features.loan.domain.Loan
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.muratcant.bookstack.features.loan.domain.LoanStatus
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.domain.MemberStatus
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
    private val loanProperties: LoanProperties
) {
    @Transactional
    fun handle(request: BorrowCopyRequest): BorrowCopyResponse {
        val member = memberRepository.findById(request.memberId)
            .orElseThrow { ResourceNotFoundException("Member not found: ${request.memberId}") }

        val copy = bookCopyRepository.findById(request.copyId)
            .orElseThrow { ResourceNotFoundException("Book copy not found: ${request.copyId}") }

        // Business Rule: Member must be ACTIVE
        if (member.status != MemberStatus.ACTIVE) {
            throw MemberNotActiveException("Member is not active: ${member.status}")
        }

        // Business Rule: Member must be checked in
        if (!visitRepository.existsByMemberIdAndCheckOutTimeIsNull(request.memberId)) {
            throw MemberNotCheckedInException("Member must be checked in to borrow a copy")
        }

        // Business Rule: Copy must be AVAILABLE
        if (copy.status != CopyStatus.AVAILABLE) {
            throw CopyNotAvailableException("Copy is not available: ${copy.status}")
        }

        // Business Rule: Copy must be BORROWABLE or BOTH
        if (copy.usageType == UsageType.READING_ROOM_ONLY) {
            throw CopyNotBorrowableException("Copy is for reading room only")
        }

        // Business Rule: Member must not exceed max active loans
        val activeLoansCount = loanRepository.countByMemberIdAndStatus(request.memberId, LoanStatus.ACTIVE)
        if (activeLoansCount >= member.maxActiveLoans) {
            throw MaxLoansExceededException("Member has reached maximum active loans limit: ${member.maxActiveLoans}")
        }

        // Create loan
        val dueDate = LocalDateTime.now().plusDays(loanProperties.defaultDurationDays.toLong())
        val loan = Loan(
            member = member,
            copy = copy,
            dueDate = dueDate
        )

        // Update copy status
        copy.status = CopyStatus.LOANED

        val savedLoan = loanRepository.save(loan)
        bookCopyRepository.save(copy)

        return savedLoan.toResponse()
    }

    private fun Loan.toResponse() = BorrowCopyResponse(
        id = id,
        memberId = member.id,
        memberName = "${member.firstName} ${member.lastName}",
        copyId = copy.id,
        bookTitle = copy.book.title,
        barcode = copy.barcode,
        borrowedAt = borrowedAt,
        dueDate = dueDate,
        status = status.name
    )
}
