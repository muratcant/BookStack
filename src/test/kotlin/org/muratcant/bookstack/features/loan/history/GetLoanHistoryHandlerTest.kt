package org.muratcant.bookstack.features.loan.history

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.muratcant.bookstack.features.loan.test.LoanBuilder
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.time.LocalDateTime
import java.util.UUID

class GetLoanHistoryHandlerTest : FunSpec({

    val loanRepository = mockk<LoanRepository>()
    val memberRepository = mockk<MemberRepository>()
    val handler = GetLoanHistoryHandler(loanRepository, memberRepository)

    beforeTest {
        clearMocks(loanRepository, memberRepository)
    }

    test("Given member with loan history When get history Then should return all loans ordered by borrowedAt desc") {
        val memberId = UUID.randomUUID()
        val activeLoan = LoanBuilder.anActiveLoan()
        val returnedLoan = LoanBuilder.aReturnedLoan()
        val overdueLoan = LoanBuilder.anOverdueLoan()

        every { memberRepository.existsById(memberId) } returns true
        every { loanRepository.findByMemberIdOrderByBorrowedAtDesc(memberId) } returns listOf(
            activeLoan,
            returnedLoan,
            overdueLoan
        )

        val response = handler.handle(memberId)

        response.loans.size shouldBe 3
        response.loans[0].id shouldBe activeLoan.id
        response.loans[0].status shouldBe "ACTIVE"
        response.loans[0].isOverdue shouldBe false
        response.loans[0].returnedAt shouldBe null
        response.loans[1].id shouldBe returnedLoan.id
        response.loans[1].status shouldBe "RETURNED"
        response.loans[1].returnedAt shouldBe returnedLoan.returnedAt
        response.loans[2].id shouldBe overdueLoan.id
        response.loans[2].status shouldBe "ACTIVE"
        response.loans[2].isOverdue shouldBe true
    }

    test("Given member with returned loan When get history Then should include return timestamp") {
        val memberId = UUID.randomUUID()
        val returnedAt = LocalDateTime.now().minusDays(1)
        val returnedLoan = LoanBuilder.aReturnedLoan(returnedAt = returnedAt)

        every { memberRepository.existsById(memberId) } returns true
        every { loanRepository.findByMemberIdOrderByBorrowedAtDesc(memberId) } returns listOf(returnedLoan)

        val response = handler.handle(memberId)

        response.loans.size shouldBe 1
        response.loans[0].status shouldBe "RETURNED"
        response.loans[0].returnedAt shouldBe returnedAt
        response.loans[0].isOverdue shouldBe false
    }

    test("Given member with overdue loan When get history Then should indicate overdue status") {
        val memberId = UUID.randomUUID()
        val overdueLoan = LoanBuilder.anOverdueLoan()

        every { memberRepository.existsById(memberId) } returns true
        every { loanRepository.findByMemberIdOrderByBorrowedAtDesc(memberId) } returns listOf(overdueLoan)

        val response = handler.handle(memberId)

        response.loans.size shouldBe 1
        response.loans[0].id shouldBe overdueLoan.id
        response.loans[0].status shouldBe "ACTIVE"
        response.loans[0].isOverdue shouldBe true
    }

    test("Given member with no loans When get history Then should return empty list") {
        val memberId = UUID.randomUUID()

        every { memberRepository.existsById(memberId) } returns true
        every { loanRepository.findByMemberIdOrderByBorrowedAtDesc(memberId) } returns emptyList()

        val response = handler.handle(memberId)

        response.loans.size shouldBe 0
    }

    test("Given non-existent member When get history Then should throw ResourceNotFoundException") {
        val memberId = UUID.randomUUID()

        every { memberRepository.existsById(memberId) } returns false

        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(memberId)
        }

        exception.message shouldBe "Member not found: $memberId"
    }

    test("Given member with multiple loans When get history Then should include all loan details") {
        val memberId = UUID.randomUUID()
        val loan1 = LoanBuilder.anActiveLoan()
        val loan2 = LoanBuilder.aReturnedLoan()

        every { memberRepository.existsById(memberId) } returns true
        every { loanRepository.findByMemberIdOrderByBorrowedAtDesc(memberId) } returns listOf(loan1, loan2)

        val response = handler.handle(memberId)

        response.loans.size shouldBe 2
        response.loans[0].copyId shouldBe loan1.bookCopy.id
        response.loans[0].bookTitle shouldBe loan1.bookCopy.book.title
        response.loans[0].bookIsbn shouldBe loan1.bookCopy.book.isbn
        response.loans[0].barcode shouldBe loan1.bookCopy.barcode
        response.loans[1].copyId shouldBe loan2.bookCopy.id
        response.loans[1].bookTitle shouldBe loan2.bookCopy.book.title
    }
})
