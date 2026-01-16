package org.muratcant.bookstack.features.loan.get

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.muratcant.bookstack.features.loan.test.LoanBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class GetLoanHandlerTest : FunSpec({

    val loanRepository = mockk<LoanRepository>()
    val handler = GetLoanHandler(loanRepository)

    beforeTest {
        clearMocks(loanRepository)
    }

    test("Given existing loan When get Then should return loan details") {
        val loanId = UUID.randomUUID()
        val loan = LoanBuilder.anActiveLoan(id = loanId)

        every { loanRepository.findById(loanId) } returns Optional.of(loan)

        val response = handler.handle(loanId)

        response.id shouldBe loanId
        response.memberId shouldBe loan.member.id
        response.memberName shouldBe "${loan.member.firstName} ${loan.member.lastName}"
        response.membershipNumber shouldBe loan.member.membershipNumber
        response.copyId shouldBe loan.bookCopy.id
        response.bookId shouldBe loan.bookCopy.book.id
        response.bookTitle shouldBe loan.bookCopy.book.title
        response.bookIsbn shouldBe loan.bookCopy.book.isbn
        response.barcode shouldBe loan.bookCopy.barcode
        response.borrowedAt shouldNotBe null
        response.dueDate shouldNotBe null
        response.status shouldBe "ACTIVE"
        response.isOverdue shouldBe false
        response.returnedAt shouldBe null
    }

    test("Given returned loan When get Then should include return timestamp") {
        val loanId = UUID.randomUUID()
        val returnedAt = LocalDateTime.now()
        val loan = LoanBuilder.aReturnedLoan(id = loanId, returnedAt = returnedAt)

        every { loanRepository.findById(loanId) } returns Optional.of(loan)

        val response = handler.handle(loanId)

        response.id shouldBe loanId
        response.status shouldBe "RETURNED"
        response.returnedAt shouldBe returnedAt
        response.isOverdue shouldBe false
    }

    test("Given overdue loan When get Then should indicate overdue status") {
        val loanId = UUID.randomUUID()
        val loan = LoanBuilder.anOverdueLoan(id = loanId)

        every { loanRepository.findById(loanId) } returns Optional.of(loan)

        val response = handler.handle(loanId)

        response.id shouldBe loanId
        response.status shouldBe "ACTIVE"
        response.isOverdue shouldBe true
    }

    test("Given non-existent loan When get Then should throw ResourceNotFoundException") {
        val loanId = UUID.randomUUID()

        every { loanRepository.findById(loanId) } returns Optional.empty()

        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(loanId)
        }

        exception.message shouldBe "Loan not found: $loanId"
    }
})
