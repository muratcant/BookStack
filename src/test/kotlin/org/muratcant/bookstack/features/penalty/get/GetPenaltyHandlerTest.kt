package org.muratcant.bookstack.features.penalty.get

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.penalty.domain.PenaltyRepository
import org.muratcant.bookstack.features.penalty.test.PenaltyBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class GetPenaltyHandlerTest : FunSpec({

    val penaltyRepository = mockk<PenaltyRepository>()
    val handler = GetPenaltyHandler(penaltyRepository)

    beforeTest {
        clearMocks(penaltyRepository)
    }

    test("Given existing penalty When get Then should return full penalty details") {
        val penaltyId = UUID.randomUUID()
        val penalty = PenaltyBuilder.anUnpaidPenalty(id = penaltyId)

        every { penaltyRepository.findById(penaltyId) } returns Optional.of(penalty)

        val response = handler.handle(penaltyId)

        response.id shouldBe penaltyId
        response.memberId shouldBe penalty.member.id
        response.memberName shouldBe "${penalty.member.firstName} ${penalty.member.lastName}"
        response.loanId shouldBe penalty.loan.id
        response.bookTitle shouldBe penalty.loan.bookCopy.book.title
        response.barcode shouldBe penalty.loan.bookCopy.barcode
        response.amount shouldBe penalty.amount
        response.daysOverdue shouldBe penalty.daysOverdue
        response.status shouldBe "UNPAID"
        response.createdAt shouldNotBe null
    }

    test("Given unpaid penalty When get Then should show paidAt as null") {
        val penaltyId = UUID.randomUUID()
        val penalty = PenaltyBuilder.anUnpaidPenalty(id = penaltyId)

        every { penaltyRepository.findById(penaltyId) } returns Optional.of(penalty)

        val response = handler.handle(penaltyId)

        response.id shouldBe penaltyId
        response.status shouldBe "UNPAID"
        response.paidAt shouldBe null
    }

    test("Given paid penalty When get Then should include paid timestamp") {
        val penaltyId = UUID.randomUUID()
        val paidAt = LocalDateTime.now()
        val penalty = PenaltyBuilder.aPaidPenalty(id = penaltyId)

        every { penaltyRepository.findById(penaltyId) } returns Optional.of(penalty)

        val response = handler.handle(penaltyId)

        response.id shouldBe penaltyId
        response.status shouldBe "PAID"
        response.paidAt shouldNotBe null
    }

    test("Given non-existent penalty When get Then should throw ResourceNotFoundException") {
        val penaltyId = UUID.randomUUID()

        every { penaltyRepository.findById(penaltyId) } returns Optional.empty()

        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(penaltyId)
        }

        exception.message shouldBe "Penalty not found: $penaltyId"
    }
})
