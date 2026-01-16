package org.muratcant.bookstack.features.loan.listactive

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.muratcant.bookstack.features.loan.domain.LoanStatus
import org.muratcant.bookstack.features.loan.test.LoanBuilder
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.time.LocalDateTime
import java.util.UUID

class GetActiveLoansHandlerTest : FunSpec({

    val loanRepository = mockk<LoanRepository>()
    val memberRepository = mockk<MemberRepository>()
    val handler = GetActiveLoansHandler(loanRepository, memberRepository)

    beforeTest {
        clearMocks(loanRepository, memberRepository)
    }

    test("Given member with active loans When get active loans Then should return all active loans ordered by borrowedAt desc") {
        val memberId = UUID.randomUUID()
        val loan1 = LoanBuilder.anActiveLoan(dueDate = LocalDateTime.now().plusDays(7))
        val loan2 = LoanBuilder.anActiveLoan(dueDate = LocalDateTime.now().plusDays(14))
        val loan3 = LoanBuilder.anActiveLoan(dueDate = LocalDateTime.now().plusDays(5))

        every { memberRepository.existsById(memberId) } returns true
        every { loanRepository.findByMemberIdAndStatusOrderByBorrowedAtDesc(memberId, LoanStatus.ACTIVE) } returns listOf(
            loan1,
            loan2,
            loan3
        )

        val response = handler.handle(memberId)

        response.loans.size shouldBe 3
        response.loans[0].id shouldBe loan1.id
        response.loans[0].bookTitle shouldBe loan1.bookCopy.book.title
        response.loans[0].barcode shouldBe loan1.bookCopy.barcode
        response.loans[0].isOverdue shouldBe false
        response.loans[1].id shouldBe loan2.id
        response.loans[2].id shouldBe loan3.id
    }

    test("Given member with overdue active loan When get active loans Then should indicate overdue status") {
        val memberId = UUID.randomUUID()
        val overdueLoan = LoanBuilder.anOverdueLoan()

        every { memberRepository.existsById(memberId) } returns true
        every { loanRepository.findByMemberIdAndStatusOrderByBorrowedAtDesc(memberId, LoanStatus.ACTIVE) } returns listOf(
            overdueLoan
        )

        val response = handler.handle(memberId)

        response.loans.size shouldBe 1
        response.loans[0].id shouldBe overdueLoan.id
        response.loans[0].isOverdue shouldBe true
    }

    test("Given member with no active loans When get active loans Then should return empty list") {
        val memberId = UUID.randomUUID()

        every { memberRepository.existsById(memberId) } returns true
        every { loanRepository.findByMemberIdAndStatusOrderByBorrowedAtDesc(memberId, LoanStatus.ACTIVE) } returns emptyList()

        val response = handler.handle(memberId)

        response.loans.size shouldBe 0
    }

    test("Given non-existent member When get active loans Then should throw ResourceNotFoundException") {
        val memberId = UUID.randomUUID()

        every { memberRepository.existsById(memberId) } returns false

        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(memberId)
        }

        exception.message shouldBe "Member not found: $memberId"
    }
})
