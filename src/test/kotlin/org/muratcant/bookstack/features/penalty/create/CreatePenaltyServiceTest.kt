package org.muratcant.bookstack.features.penalty.create

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.loan.test.LoanBuilder
import org.muratcant.bookstack.features.penalty.config.PenaltyProperties
import org.muratcant.bookstack.features.penalty.domain.PenaltyRepository
import java.math.BigDecimal

class CreatePenaltyServiceTest : FunSpec({

    val penaltyRepository = mockk<PenaltyRepository>()
    val penaltyProperties = PenaltyProperties(
        dailyFee = BigDecimal("1.00"),
        blockingThreshold = BigDecimal("10.00")
    )
    val service = CreatePenaltyService(penaltyRepository, penaltyProperties)

    beforeTest {
        clearMocks(penaltyRepository)
    }

    test("Given overdue loan When createIfOverdue Then should create penalty with correct amount") {
        // Given
        val loan = LoanBuilder.aReturnedLoan()
        val daysOverdue = 5

        every { penaltyRepository.existsByLoanId(loan.id) } returns false
        every { penaltyRepository.save(any()) } answers { firstArg() }

        // When
        val penalty = service.createIfOverdue(loan, daysOverdue)

        // Then
        penalty shouldNotBe null
        penalty!!.amount shouldBe BigDecimal("5.00")
        penalty.daysOverdue shouldBe 5
        penalty.member shouldBe loan.member
        penalty.loan shouldBe loan
        verify(exactly = 1) { penaltyRepository.save(any()) }
    }

    test("Given zero days overdue When createIfOverdue Then should return null") {
        // Given
        val loan = LoanBuilder.aReturnedLoan()
        val daysOverdue = 0

        // When
        val penalty = service.createIfOverdue(loan, daysOverdue)

        // Then
        penalty shouldBe null
        verify(exactly = 0) { penaltyRepository.save(any()) }
    }

    test("Given negative days overdue When createIfOverdue Then should return null") {
        // Given
        val loan = LoanBuilder.aReturnedLoan()
        val daysOverdue = -1

        // When
        val penalty = service.createIfOverdue(loan, daysOverdue)

        // Then
        penalty shouldBe null
        verify(exactly = 0) { penaltyRepository.save(any()) }
    }

    test("Given loan already has penalty When createIfOverdue Then should return null") {
        // Given
        val loan = LoanBuilder.aReturnedLoan()
        val daysOverdue = 5

        every { penaltyRepository.existsByLoanId(loan.id) } returns true

        // When
        val penalty = service.createIfOverdue(loan, daysOverdue)

        // Then
        penalty shouldBe null
        verify(exactly = 0) { penaltyRepository.save(any()) }
    }

    test("Given 10 days overdue When createIfOverdue Then should calculate correct amount") {
        // Given
        val loan = LoanBuilder.aReturnedLoan()
        val daysOverdue = 10

        every { penaltyRepository.existsByLoanId(loan.id) } returns false
        every { penaltyRepository.save(any()) } answers { firstArg() }

        // When
        val penalty = service.createIfOverdue(loan, daysOverdue)

        // Then
        penalty shouldNotBe null
        penalty!!.amount shouldBe BigDecimal("10.00")
    }
})
