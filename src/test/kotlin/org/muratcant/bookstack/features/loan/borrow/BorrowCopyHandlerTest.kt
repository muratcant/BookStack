package org.muratcant.bookstack.features.loan.borrow

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.bookcopy.test.BookCopyBuilder
import org.muratcant.bookstack.features.loan.config.LoanProperties
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.muratcant.bookstack.features.loan.domain.LoanStatus
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.features.penalty.config.PenaltyProperties
import org.muratcant.bookstack.features.penalty.domain.PenaltyRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import org.muratcant.bookstack.features.reservation.process.ProcessReservationService
import org.muratcant.bookstack.features.reservation.test.ReservationBuilder
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.muratcant.bookstack.shared.exception.*
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

class BorrowCopyHandlerTest : FunSpec({

    val loanRepository = mockk<LoanRepository>()
    val memberRepository = mockk<MemberRepository>()
    val bookCopyRepository = mockk<BookCopyRepository>()
    val visitRepository = mockk<VisitRepository>()
    val penaltyRepository = mockk<PenaltyRepository>()
    val reservationRepository = mockk<ReservationRepository>()
    val processReservationService = mockk<ProcessReservationService>()
    val loanProperties = LoanProperties(defaultDurationDays = 14, maxExtensions = 2, extensionDays = 7)
    val penaltyProperties = PenaltyProperties(dailyFee = BigDecimal("1.00"), blockingThreshold = BigDecimal("10.00"))
    
    val handler = BorrowCopyHandler(
        loanRepository,
        memberRepository,
        bookCopyRepository,
        visitRepository,
        penaltyRepository,
        reservationRepository,
        processReservationService,
        loanProperties,
        penaltyProperties
    )

    beforeTest {
        clearMocks(loanRepository, memberRepository, bookCopyRepository, visitRepository, penaltyRepository, reservationRepository, processReservationService)
    }

    test("Given active checked-in member and available copy When borrow Then should create loan") {
        // Given
        val memberId = UUID.randomUUID()
        val copyId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val copy = BookCopyBuilder.anAvailableCopy(id = copyId)
        val request = BorrowCopyRequest(memberId = memberId, copyId = copyId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookCopyRepository.findById(copyId) } returns Optional.of(copy)
        every { visitRepository.existsByMemberIdAndCheckOutTimeIsNull(memberId) } returns true
        every { penaltyRepository.sumUnpaidAmountByMemberId(memberId) } returns BigDecimal.ZERO
        every { loanRepository.countByMemberIdAndStatus(memberId, LoanStatus.ACTIVE) } returns 0
        every { loanRepository.save(any()) } answers { firstArg() }
        every { bookCopyRepository.save(any()) } answers { firstArg() }
        every { processReservationService.fulfillReservation(copyId) } returns null

        // When
        val response = handler.handle(request)

        // Then
        response.id shouldNotBe null
        response.memberId shouldBe memberId
        response.copyId shouldBe copyId
        response.status shouldBe "ACTIVE"
        verify(exactly = 1) { loanRepository.save(any()) }
        verify(exactly = 1) { bookCopyRepository.save(any()) }
    }

    test("Given suspended member When borrow Then should throw MemberNotActiveException") {
        // Given
        val memberId = UUID.randomUUID()
        val copyId = UUID.randomUUID()
        val member = MemberBuilder.aSuspendedMember(id = memberId)
        val copy = BookCopyBuilder.anAvailableCopy(id = copyId)
        val request = BorrowCopyRequest(memberId = memberId, copyId = copyId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookCopyRepository.findById(copyId) } returns Optional.of(copy)

        // When & Then
        val exception = shouldThrow<MemberNotActiveException> {
            handler.handle(request)
        }

        exception.message shouldBe "Member is not active: SUSPENDED"
        verify(exactly = 0) { loanRepository.save(any()) }
    }

    test("Given member not checked in When borrow Then should throw MemberNotCheckedInException") {
        // Given
        val memberId = UUID.randomUUID()
        val copyId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val copy = BookCopyBuilder.anAvailableCopy(id = copyId)
        val request = BorrowCopyRequest(memberId = memberId, copyId = copyId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookCopyRepository.findById(copyId) } returns Optional.of(copy)
        every { visitRepository.existsByMemberIdAndCheckOutTimeIsNull(memberId) } returns false

        // When & Then
        val exception = shouldThrow<MemberNotCheckedInException> {
            handler.handle(request)
        }

        exception.message shouldBe "Member must be checked in to borrow a copy"
        verify(exactly = 0) { loanRepository.save(any()) }
    }

    test("Given member with unpaid penalties above threshold When borrow Then should throw UnpaidPenaltiesException") {
        // Given
        val memberId = UUID.randomUUID()
        val copyId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val copy = BookCopyBuilder.anAvailableCopy(id = copyId)
        val request = BorrowCopyRequest(memberId = memberId, copyId = copyId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookCopyRepository.findById(copyId) } returns Optional.of(copy)
        every { visitRepository.existsByMemberIdAndCheckOutTimeIsNull(memberId) } returns true
        every { penaltyRepository.sumUnpaidAmountByMemberId(memberId) } returns BigDecimal("15.00")

        // When & Then
        val exception = shouldThrow<UnpaidPenaltiesException> {
            handler.handle(request)
        }

        exception.message shouldBe "Member has unpaid penalties (15.00) above blocking threshold (10.00)"
        verify(exactly = 0) { loanRepository.save(any()) }
    }

    test("Given loaned copy When borrow Then should throw CopyNotAvailableException") {
        // Given
        val memberId = UUID.randomUUID()
        val copyId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val copy = BookCopyBuilder.aLoanedCopy(id = copyId)
        val request = BorrowCopyRequest(memberId = memberId, copyId = copyId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookCopyRepository.findById(copyId) } returns Optional.of(copy)
        every { visitRepository.existsByMemberIdAndCheckOutTimeIsNull(memberId) } returns true
        every { penaltyRepository.sumUnpaidAmountByMemberId(memberId) } returns BigDecimal.ZERO

        // When & Then
        val exception = shouldThrow<CopyNotAvailableException> {
            handler.handle(request)
        }

        exception.message shouldBe "Copy is not available: LOANED"
        verify(exactly = 0) { loanRepository.save(any()) }
    }

    test("Given reading room only copy When borrow Then should throw CopyNotBorrowableException") {
        // Given
        val memberId = UUID.randomUUID()
        val copyId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val copy = BookCopyBuilder.aReadingRoomOnlyCopy(id = copyId)
        val request = BorrowCopyRequest(memberId = memberId, copyId = copyId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookCopyRepository.findById(copyId) } returns Optional.of(copy)
        every { visitRepository.existsByMemberIdAndCheckOutTimeIsNull(memberId) } returns true
        every { penaltyRepository.sumUnpaidAmountByMemberId(memberId) } returns BigDecimal.ZERO

        // When & Then
        val exception = shouldThrow<CopyNotBorrowableException> {
            handler.handle(request)
        }

        exception.message shouldBe "Copy is for reading room only"
        verify(exactly = 0) { loanRepository.save(any()) }
    }

    test("Given member at max loans limit When borrow Then should throw MaxLoansExceededException") {
        // Given
        val memberId = UUID.randomUUID()
        val copyId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val copy = BookCopyBuilder.anAvailableCopy(id = copyId)
        val request = BorrowCopyRequest(memberId = memberId, copyId = copyId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookCopyRepository.findById(copyId) } returns Optional.of(copy)
        every { visitRepository.existsByMemberIdAndCheckOutTimeIsNull(memberId) } returns true
        every { penaltyRepository.sumUnpaidAmountByMemberId(memberId) } returns BigDecimal.ZERO
        every { loanRepository.countByMemberIdAndStatus(memberId, LoanStatus.ACTIVE) } returns 5

        // When & Then
        val exception = shouldThrow<MaxLoansExceededException> {
            handler.handle(request)
        }

        exception.message shouldBe "Member has reached maximum active loans limit: 5"
        verify(exactly = 0) { loanRepository.save(any()) }
    }

    test("Given non-existing member When borrow Then should throw ResourceNotFoundException") {
        // Given
        val memberId = UUID.randomUUID()
        val copyId = UUID.randomUUID()
        val request = BorrowCopyRequest(memberId = memberId, copyId = copyId)

        every { memberRepository.findById(memberId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(request)
        }

        exception.message shouldBe "Member not found: $memberId"
        verify(exactly = 0) { loanRepository.save(any()) }
    }

    test("Given non-existing copy When borrow Then should throw ResourceNotFoundException") {
        // Given
        val memberId = UUID.randomUUID()
        val copyId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val request = BorrowCopyRequest(memberId = memberId, copyId = copyId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookCopyRepository.findById(copyId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(request)
        }

        exception.message shouldBe "Book copy not found: $copyId"
        verify(exactly = 0) { loanRepository.save(any()) }
    }

    test("Given valid borrow When borrow Then should update copy status to LOANED") {
        // Given
        val memberId = UUID.randomUUID()
        val copyId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val copy = BookCopyBuilder.anAvailableCopy(id = copyId)
        val request = BorrowCopyRequest(memberId = memberId, copyId = copyId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookCopyRepository.findById(copyId) } returns Optional.of(copy)
        every { visitRepository.existsByMemberIdAndCheckOutTimeIsNull(memberId) } returns true
        every { penaltyRepository.sumUnpaidAmountByMemberId(memberId) } returns BigDecimal.ZERO
        every { loanRepository.countByMemberIdAndStatus(memberId, LoanStatus.ACTIVE) } returns 0
        every { loanRepository.save(any()) } answers { firstArg() }
        every { bookCopyRepository.save(any()) } answers { firstArg() }
        every { processReservationService.fulfillReservation(copyId) } returns null

        // When
        handler.handle(request)

        // Then
        copy.status shouldBe CopyStatus.LOANED
    }

    test("Given ON_HOLD copy and reservation holder When borrow Then should allow and fulfill reservation") {
        // Given
        val memberId = UUID.randomUUID()
        val copyId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val copy = BookCopyBuilder.anOnHoldCopy(id = copyId)
        val reservation = ReservationBuilder.aReadyForPickupReservation(member = member, copy = copy)
        val request = BorrowCopyRequest(memberId = memberId, copyId = copyId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookCopyRepository.findById(copyId) } returns Optional.of(copy)
        every { visitRepository.existsByMemberIdAndCheckOutTimeIsNull(memberId) } returns true
        every { penaltyRepository.sumUnpaidAmountByMemberId(memberId) } returns BigDecimal.ZERO
        every { reservationRepository.findByCopyIdAndStatus(copyId, ReservationStatus.READY_FOR_PICKUP) } returns reservation
        every { loanRepository.countByMemberIdAndStatus(memberId, LoanStatus.ACTIVE) } returns 0
        every { loanRepository.save(any()) } answers { firstArg() }
        every { bookCopyRepository.save(any()) } answers { firstArg() }
        every { processReservationService.fulfillReservation(copyId) } returns reservation

        // When
        val response = handler.handle(request)

        // Then
        response.id shouldNotBe null
        copy.status shouldBe CopyStatus.LOANED
        verify(exactly = 1) { processReservationService.fulfillReservation(copyId) }
    }

    test("Given ON_HOLD copy and not reservation holder When borrow Then should throw CopyNotAvailableException") {
        // Given
        val memberId = UUID.randomUUID()
        val otherMemberId = UUID.randomUUID()
        val copyId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val otherMember = MemberBuilder.anActiveMember(id = otherMemberId)
        val copy = BookCopyBuilder.anOnHoldCopy(id = copyId)
        val reservation = ReservationBuilder.aReadyForPickupReservation(member = otherMember, copy = copy)
        val request = BorrowCopyRequest(memberId = memberId, copyId = copyId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookCopyRepository.findById(copyId) } returns Optional.of(copy)
        every { visitRepository.existsByMemberIdAndCheckOutTimeIsNull(memberId) } returns true
        every { penaltyRepository.sumUnpaidAmountByMemberId(memberId) } returns BigDecimal.ZERO
        every { reservationRepository.findByCopyIdAndStatus(copyId, ReservationStatus.READY_FOR_PICKUP) } returns reservation

        // When & Then
        val exception = shouldThrow<CopyNotAvailableException> {
            handler.handle(request)
        }

        exception.message shouldBe "Copy is on hold for another member's reservation"
        verify(exactly = 0) { loanRepository.save(any()) }
    }
})
