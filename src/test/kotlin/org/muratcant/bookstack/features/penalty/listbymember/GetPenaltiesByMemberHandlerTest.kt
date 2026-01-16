package org.muratcant.bookstack.features.penalty.listbymember

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.features.penalty.domain.PenaltyRepository
import org.muratcant.bookstack.features.penalty.test.PenaltyBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.math.BigDecimal
import java.util.UUID

class GetPenaltiesByMemberHandlerTest : FunSpec({

    val penaltyRepository = mockk<PenaltyRepository>()
    val memberRepository = mockk<MemberRepository>()
    val handler = GetPenaltiesByMemberHandler(penaltyRepository, memberRepository)

    beforeTest {
        clearMocks(penaltyRepository, memberRepository)
    }

    test("Given member with penalties When get by member Then should return penalties with total unpaid amount") {
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val unpaidPenalty1 = PenaltyBuilder.anUnpaidPenalty(member = member, amount = BigDecimal("10.00"))
        val unpaidPenalty2 = PenaltyBuilder.anUnpaidPenalty(member = member, amount = BigDecimal("5.00"))
        val paidPenalty = PenaltyBuilder.aPaidPenalty(member = member, amount = BigDecimal("3.00"))

        every { memberRepository.existsById(memberId) } returns true
        every { penaltyRepository.findByMemberIdOrderByCreatedAtDesc(memberId) } returns listOf(
            unpaidPenalty1,
            unpaidPenalty2,
            paidPenalty
        )
        every { penaltyRepository.sumUnpaidAmountByMemberId(memberId) } returns BigDecimal("15.00")

        val response = handler.handle(memberId)

        response.totalUnpaidAmount shouldBe BigDecimal("15.00")
        response.penalties.size shouldBe 3
        response.penalties[0].id shouldBe unpaidPenalty1.id
        response.penalties[0].status shouldBe "UNPAID"
        response.penalties[0].amount shouldBe BigDecimal("10.00")
        response.penalties[1].id shouldBe unpaidPenalty2.id
        response.penalties[1].status shouldBe "UNPAID"
        response.penalties[2].id shouldBe paidPenalty.id
        response.penalties[2].status shouldBe "PAID"
    }

    test("Given member with no penalties When get by member Then should return empty list with zero total") {
        val memberId = UUID.randomUUID()

        every { memberRepository.existsById(memberId) } returns true
        every { penaltyRepository.findByMemberIdOrderByCreatedAtDesc(memberId) } returns emptyList()
        every { penaltyRepository.sumUnpaidAmountByMemberId(memberId) } returns BigDecimal.ZERO

        val response = handler.handle(memberId)

        response.totalUnpaidAmount shouldBe BigDecimal.ZERO
        response.penalties.size shouldBe 0
    }

    test("Given member with only paid penalties When get by member Then should have zero total unpaid") {
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val paidPenalty1 = PenaltyBuilder.aPaidPenalty(member = member, amount = BigDecimal("5.00"))
        val paidPenalty2 = PenaltyBuilder.aPaidPenalty(member = member, amount = BigDecimal("3.00"))

        every { memberRepository.existsById(memberId) } returns true
        every { penaltyRepository.findByMemberIdOrderByCreatedAtDesc(memberId) } returns listOf(
            paidPenalty1,
            paidPenalty2
        )
        every { penaltyRepository.sumUnpaidAmountByMemberId(memberId) } returns BigDecimal.ZERO

        val response = handler.handle(memberId)

        response.totalUnpaidAmount shouldBe BigDecimal.ZERO
        response.penalties.size shouldBe 2
        response.penalties[0].status shouldBe "PAID"
        response.penalties[1].status shouldBe "PAID"
    }

    test("Given non-existent member When get penalties Then should throw ResourceNotFoundException") {
        val memberId = UUID.randomUUID()

        every { memberRepository.existsById(memberId) } returns false

        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(memberId)
        }

        exception.message shouldBe "Member not found: $memberId"
    }

    test("Given member with penalties When get by member Then should include all penalty details") {
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val penalty = PenaltyBuilder.anUnpaidPenalty(member = member, amount = BigDecimal("7.00"), daysOverdue = 7)

        every { memberRepository.existsById(memberId) } returns true
        every { penaltyRepository.findByMemberIdOrderByCreatedAtDesc(memberId) } returns listOf(penalty)
        every { penaltyRepository.sumUnpaidAmountByMemberId(memberId) } returns BigDecimal("7.00")

        val response = handler.handle(memberId)

        response.penalties.size shouldBe 1
        response.penalties[0].loanId shouldBe penalty.loan.id
        response.penalties[0].bookTitle shouldBe penalty.loan.bookCopy.book.title
        response.penalties[0].barcode shouldBe penalty.loan.bookCopy.barcode
        response.penalties[0].amount shouldBe penalty.amount
        response.penalties[0].daysOverdue shouldBe penalty.daysOverdue
    }
})
