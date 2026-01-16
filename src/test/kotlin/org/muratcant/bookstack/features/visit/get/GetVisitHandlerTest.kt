package org.muratcant.bookstack.features.visit.get

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.muratcant.bookstack.features.visit.test.VisitBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class GetVisitHandlerTest : FunSpec({

    val visitRepository = mockk<VisitRepository>()
    val handler = GetVisitHandler(visitRepository)

    beforeTest {
        clearMocks(visitRepository)
    }

    test("Given active visit When get Then should show active status") {
        val visitId = UUID.randomUUID()
        val visit = VisitBuilder.anActiveVisit(id = visitId)

        every { visitRepository.findById(visitId) } returns Optional.of(visit)

        val response = handler.handle(visitId)

        response.id shouldBe visitId
        response.checkOutTime shouldBe null
        response.isActive shouldBe true
    }

    test("Given completed visit When get Then should include checkout timestamp") {
        val visitId = UUID.randomUUID()
        val visit = VisitBuilder.aCompletedVisit(id = visitId)

        every { visitRepository.findById(visitId) } returns Optional.of(visit)

        val response = handler.handle(visitId)

        response.id shouldBe visitId
        response.checkOutTime shouldNotBe null
        response.isActive shouldBe false
    }

    test("Given visit When get Then should include all member details") {
        val visitId = UUID.randomUUID()
        val visit = VisitBuilder.anActiveVisit(id = visitId)

        every { visitRepository.findById(visitId) } returns Optional.of(visit)

        val response = handler.handle(visitId)

        response.id shouldBe visitId
        response.memberId shouldBe visit.member.id
        response.memberName shouldBe "${visit.member.firstName} ${visit.member.lastName}"
        response.membershipNumber shouldBe visit.member.membershipNumber
        response.checkInTime shouldNotBe null
    }

    test("Given non-existent visit When get Then should throw ResourceNotFoundException") {
        val visitId = UUID.randomUUID()

        every { visitRepository.findById(visitId) } returns Optional.empty()

        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(visitId)
        }

        exception.message shouldBe "Visit not found: $visitId"
    }
})
