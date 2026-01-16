package org.muratcant.bookstack.features.penalty.list

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.penalty.domain.PenaltyRepository
import org.muratcant.bookstack.features.penalty.domain.PenaltyStatus
import org.muratcant.bookstack.features.penalty.test.PenaltyBuilder
import org.springframework.data.domain.Sort
import java.math.BigDecimal
import java.time.LocalDateTime

class ListPenaltiesHandlerTest : FunSpec({

    val penaltyRepository = mockk<PenaltyRepository>()
    val handler = ListPenaltiesHandler(penaltyRepository)

    beforeTest {
        clearMocks(penaltyRepository)
    }

    test("Given multiple penalties exist When list all Then should return all penalties ordered by createdAt desc") {
        val penalty1 = PenaltyBuilder.anUnpaidPenalty(amount = BigDecimal("10.00"), daysOverdue = 10)
        val penalty2 = PenaltyBuilder.aPaidPenalty(amount = BigDecimal("5.00"), daysOverdue = 5)
        val penalty3 = PenaltyBuilder.aPenalty(
            amount = BigDecimal("3.00"),
            daysOverdue = 3,
            status = PenaltyStatus.WAIVED
        )

        every {
            penaltyRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
        } returns listOf(penalty1, penalty2, penalty3)

        val response = handler.handle()

        response.penalties.size shouldBe 3
        response.penalties[0].id shouldBe penalty1.id
        response.penalties[0].status shouldBe "UNPAID"
        response.penalties[1].id shouldBe penalty2.id
        response.penalties[1].status shouldBe "PAID"
        response.penalties[2].id shouldBe penalty3.id
        response.penalties[2].status shouldBe "WAIVED"
    }

    test("Given penalties with different statuses When list Then should show correct status for each") {
        val unpaidPenalty = PenaltyBuilder.anUnpaidPenalty()
        val paidPenalty = PenaltyBuilder.aPaidPenalty()
        val waivedPenalty = PenaltyBuilder.aPenalty(status = PenaltyStatus.WAIVED)

        every {
            penaltyRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
        } returns listOf(unpaidPenalty, paidPenalty, waivedPenalty)

        val response = handler.handle()

        response.penalties[0].status shouldBe "UNPAID"
        response.penalties[1].status shouldBe "PAID"
        response.penalties[2].status shouldBe "WAIVED"
    }

    test("Given no penalties When list Then should return empty list") {
        every {
            penaltyRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
        } returns emptyList()

        val response = handler.handle()

        response.penalties.size shouldBe 0
    }

    test("Given multiple penalties When list Then should include all details") {
        val penalty1 = PenaltyBuilder.anUnpaidPenalty(amount = BigDecimal("8.00"), daysOverdue = 8)
        val penalty2 = PenaltyBuilder.aPaidPenalty(amount = BigDecimal("6.00"), daysOverdue = 6)

        every {
            penaltyRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
        } returns listOf(penalty1, penalty2)

        val response = handler.handle()

        response.penalties.size shouldBe 2
        response.penalties[0].memberId shouldBe penalty1.member.id
        response.penalties[0].memberName shouldBe "${penalty1.member.firstName} ${penalty1.member.lastName}"
        response.penalties[0].membershipNumber shouldBe penalty1.member.membershipNumber
        response.penalties[0].bookTitle shouldBe penalty1.loan.bookCopy.book.title
        response.penalties[0].barcode shouldBe penalty1.loan.bookCopy.barcode
        response.penalties[0].amount shouldBe penalty1.amount
        response.penalties[0].daysOverdue shouldBe penalty1.daysOverdue
        response.penalties[1].amount shouldBe penalty2.amount
        response.penalties[1].daysOverdue shouldBe penalty2.daysOverdue
    }
})
