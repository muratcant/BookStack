package org.muratcant.bookstack.features.penalty.pay

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.penalty.domain.PenaltyRepository
import org.muratcant.bookstack.features.penalty.domain.PenaltyStatus
import org.muratcant.bookstack.features.penalty.test.PenaltyBuilder
import org.muratcant.bookstack.shared.exception.BusinessRuleException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class PayPenaltyHandlerTest : FunSpec({

    val penaltyRepository = mockk<PenaltyRepository>()
    val handler = PayPenaltyHandler(penaltyRepository)

    beforeTest {
        clearMocks(penaltyRepository)
    }

    test("Given unpaid penalty When pay Then should mark as paid") {
        // Given
        val penaltyId = UUID.randomUUID()
        val penalty = PenaltyBuilder.anUnpaidPenalty(id = penaltyId)

        every { penaltyRepository.findById(penaltyId) } returns Optional.of(penalty)
        every { penaltyRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(penaltyId)

        // Then
        response.id shouldBe penaltyId
        response.status shouldBe "PAID"
        response.paidAt shouldNotBe null
        penalty.status shouldBe PenaltyStatus.PAID
        verify(exactly = 1) { penaltyRepository.save(any()) }
    }

    test("Given already paid penalty When pay Then should throw BusinessRuleException") {
        // Given
        val penaltyId = UUID.randomUUID()
        val penalty = PenaltyBuilder.aPaidPenalty(id = penaltyId)

        every { penaltyRepository.findById(penaltyId) } returns Optional.of(penalty)

        // When & Then
        val exception = shouldThrow<BusinessRuleException> {
            handler.handle(penaltyId)
        }

        exception.message shouldBe "Penalty is already paid"
        verify(exactly = 0) { penaltyRepository.save(any()) }
    }

    test("Given waived penalty When pay Then should throw BusinessRuleException") {
        // Given
        val penaltyId = UUID.randomUUID()
        val penalty = PenaltyBuilder.aPenalty(id = penaltyId, status = PenaltyStatus.WAIVED)

        every { penaltyRepository.findById(penaltyId) } returns Optional.of(penalty)

        // When & Then
        val exception = shouldThrow<BusinessRuleException> {
            handler.handle(penaltyId)
        }

        exception.message shouldBe "Penalty is already waived"
        verify(exactly = 0) { penaltyRepository.save(any()) }
    }

    test("Given non-existing penalty When pay Then should throw ResourceNotFoundException") {
        // Given
        val penaltyId = UUID.randomUUID()

        every { penaltyRepository.findById(penaltyId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(penaltyId)
        }

        exception.message shouldBe "Penalty not found: $penaltyId"
        verify(exactly = 0) { penaltyRepository.save(any()) }
    }
})
