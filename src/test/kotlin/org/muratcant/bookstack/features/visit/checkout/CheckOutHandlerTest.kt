package org.muratcant.bookstack.features.visit.checkout

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.muratcant.bookstack.features.visit.test.VisitBuilder
import org.muratcant.bookstack.shared.exception.BusinessRuleException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class CheckOutHandlerTest : FunSpec({

    val visitRepository = mockk<VisitRepository>()
    val handler = CheckOutHandler(visitRepository)

    beforeTest {
        clearMocks(visitRepository)
    }

    test("Given active visit When check-out Then should set checkOutTime") {
        // Given
        val visitId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(firstName = "John", lastName = "Doe")
        val visit = VisitBuilder.anActiveVisit(id = visitId, member = member)

        every { visitRepository.findById(visitId) } returns Optional.of(visit)
        every { visitRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(visitId)

        // Then
        response.id shouldBe visitId
        response.memberName shouldBe "John Doe"
        response.checkOutTime shouldNotBe null
        verify(exactly = 1) { visitRepository.save(any()) }
    }

    test("Given already checked out visit When check-out Then should throw BusinessRuleException") {
        // Given
        val visitId = UUID.randomUUID()
        val visit = VisitBuilder.aCompletedVisit(id = visitId)

        every { visitRepository.findById(visitId) } returns Optional.of(visit)

        // When & Then
        val exception = shouldThrow<BusinessRuleException> {
            handler.handle(visitId)
        }

        exception.message shouldBe "Visit is already checked out"
        verify(exactly = 0) { visitRepository.save(any()) }
    }

    test("Given non-existing visit When check-out Then should throw ResourceNotFoundException") {
        // Given
        val visitId = UUID.randomUUID()

        every { visitRepository.findById(visitId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(visitId)
        }

        exception.message shouldBe "Visit not found: $visitId"
        verify(exactly = 0) { visitRepository.save(any()) }
    }

    test("Given active visit When check-out Then should preserve member info") {
        // Given
        val visitId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId, firstName = "Jane", lastName = "Smith")
        val visit = VisitBuilder.anActiveVisit(id = visitId, member = member)

        every { visitRepository.findById(visitId) } returns Optional.of(visit)
        every { visitRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(visitId)

        // Then
        response.memberId shouldBe memberId
        response.memberName shouldBe "Jane Smith"
        response.checkInTime shouldBe visit.checkInTime
    }
})
