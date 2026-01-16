package org.muratcant.bookstack.features.loan.returnloan

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.muratcant.bookstack.features.loan.test.LoanBuilder
import org.muratcant.bookstack.features.penalty.create.CreatePenaltyService
import org.muratcant.bookstack.features.penalty.test.PenaltyBuilder
import org.muratcant.bookstack.features.reservation.process.ProcessReservationService
import org.muratcant.bookstack.features.reservation.test.ReservationBuilder
import org.muratcant.bookstack.shared.exception.BusinessRuleException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class ReturnCopyHandlerTest : FunSpec({

    val loanRepository = mockk<LoanRepository>()
    val bookCopyRepository = mockk<BookCopyRepository>()
    val createPenaltyService = mockk<CreatePenaltyService>()
    val processReservationService = mockk<ProcessReservationService>()
    val handler = ReturnCopyHandler(loanRepository, bookCopyRepository, createPenaltyService, processReservationService)

    beforeTest {
        clearMocks(loanRepository, bookCopyRepository, createPenaltyService, processReservationService)
    }

    test("Given active loan When return Then should mark as returned and update copy status") {
        // Given
        val loanId = UUID.randomUUID()
        val loan = LoanBuilder.anActiveLoan(id = loanId)

        every { loanRepository.findById(loanId) } returns Optional.of(loan)
        every { loanRepository.save(any()) } answers { firstArg() }
        every { bookCopyRepository.save(any()) } answers { firstArg() }
        every { createPenaltyService.createIfOverdue(any(), any()) } returns null
        every { processReservationService.processAfterReturn(any()) } returns null

        // When
        val response = handler.handle(loanId)

        // Then
        response.id shouldBe loanId
        response.status shouldBe "RETURNED"
        response.returnedAt shouldNotBe null
        response.reservationAssigned shouldBe false
        response.memberId shouldBe loan.member.id
        response.memberName shouldBe "${loan.member.firstName} ${loan.member.lastName}"
        response.bookTitle shouldBe loan.bookCopy.book.title
        response.barcode shouldBe loan.bookCopy.barcode
        response.borrowedAt shouldBe loan.borrowedAt
        response.dueDate shouldBe loan.dueDate
        response.isOverdue shouldBe false

        verify(exactly = 1) { loanRepository.save(any()) }
        verify(exactly = 1) { bookCopyRepository.save(any()) }
    }

    test("Given already returned loan When return Then should throw BusinessRuleException") {
        // Given
        val loanId = UUID.randomUUID()
        val loan = LoanBuilder.aReturnedLoan(id = loanId)

        every { loanRepository.findById(loanId) } returns Optional.of(loan)

        // When & Then
        val exception = shouldThrow<BusinessRuleException> {
            handler.handle(loanId)
        }

        exception.message shouldBe "Loan is not active: RETURNED"
        verify(exactly = 0) { loanRepository.save(any()) }
    }

    test("Given non-existing loan When return Then should throw ResourceNotFoundException") {
        // Given
        val loanId = UUID.randomUUID()

        every { loanRepository.findById(loanId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(loanId)
        }

        exception.message shouldBe "Loan not found: $loanId"
        verify(exactly = 0) { loanRepository.save(any()) }
    }

    test("Given overdue loan When return Then should calculate days overdue and create penalty") {
        // Given
        val loanId = UUID.randomUUID()
        val dueDate = LocalDateTime.now().minusDays(5)
        val loan = LoanBuilder.anOverdueLoan(id = loanId, dueDate = dueDate)
        val penalty = PenaltyBuilder.anUnpaidPenalty(loan = loan, amount = BigDecimal("5.00"), daysOverdue = 5)

        every { loanRepository.findById(loanId) } returns Optional.of(loan)
        every { loanRepository.save(any()) } answers { firstArg() }
        every { bookCopyRepository.save(any()) } answers { firstArg() }
        every { createPenaltyService.createIfOverdue(any(), 5) } returns penalty
        every { processReservationService.processAfterReturn(any()) } returns null

        // When
        val response = handler.handle(loanId)

        // Then
        response.isOverdue shouldBe true
        response.daysOverdue shouldNotBe null
        response.daysOverdue!! shouldBe 5
        response.penaltyId shouldBe penalty.id
        response.penaltyAmount shouldBe BigDecimal("5.00")
        verify(exactly = 1) { createPenaltyService.createIfOverdue(any(), 5) }
    }

    test("Given on-time loan When return Then should have no days overdue and no penalty") {
        // Given
        val loanId = UUID.randomUUID()
        val dueDate = LocalDateTime.now().plusDays(3)
        val loan = LoanBuilder.anActiveLoan(id = loanId, dueDate = dueDate)

        every { loanRepository.findById(loanId) } returns Optional.of(loan)
        every { loanRepository.save(any()) } answers { firstArg() }
        every { bookCopyRepository.save(any()) } answers { firstArg() }
        every { createPenaltyService.createIfOverdue(any(), any()) } returns null
        every { processReservationService.processAfterReturn(any()) } returns null

        // When
        val response = handler.handle(loanId)

        // Then
        response.isOverdue shouldBe false
        response.daysOverdue shouldBe null
        response.penaltyId shouldBe null
        response.penaltyAmount shouldBe null
        response.reservationAssigned shouldBe false
    }

    test("Given loan return with waiting reservation When return Then should process reservation") {
        // Given
        val loanId = UUID.randomUUID()
        val loan = LoanBuilder.anActiveLoan(id = loanId)
        val reservation = ReservationBuilder.aWaitingReservation(book = loan.bookCopy.book)

        every { loanRepository.findById(loanId) } returns Optional.of(loan)
        every { loanRepository.save(any()) } answers { firstArg() }
        every { bookCopyRepository.save(any()) } answers { firstArg() }
        every { createPenaltyService.createIfOverdue(any(), any()) } returns null
        every { processReservationService.processAfterReturn(any()) } returns reservation

        // When
        val response = handler.handle(loanId)

        // Then
        response.reservationAssigned shouldBe true
        response.reservationId shouldBe reservation.id
        verify(exactly = 1) { processReservationService.processAfterReturn(any()) }
    }
})
