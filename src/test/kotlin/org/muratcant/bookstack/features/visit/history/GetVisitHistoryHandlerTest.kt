package org.muratcant.bookstack.features.visit.history

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.muratcant.bookstack.features.visit.test.VisitBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.time.LocalDateTime
import java.util.UUID

class GetVisitHistoryHandlerTest : FunSpec({

    val visitRepository = mockk<VisitRepository>()
    val memberRepository = mockk<MemberRepository>()
    val handler = GetVisitHistoryHandler(visitRepository, memberRepository)

    beforeTest {
        clearMocks(visitRepository, memberRepository)
    }

    test("Given member with visit history When get history Then should return all visits ordered by checkInTime desc") {
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val completedVisit1 = VisitBuilder.aCompletedVisit(
            member = member,
            checkInTime = LocalDateTime.now().minusDays(2),
            checkOutTime = LocalDateTime.now().minusDays(2).plusHours(2)
        )
        val completedVisit2 = VisitBuilder.aCompletedVisit(
            member = member,
            checkInTime = LocalDateTime.now().minusDays(1),
            checkOutTime = LocalDateTime.now().minusDays(1).plusHours(3)
        )
        val activeVisit = VisitBuilder.anActiveVisit(member = member)

        every { memberRepository.existsById(memberId) } returns true
        every { visitRepository.findByMemberIdOrderByCheckInTimeDesc(memberId) } returns listOf(
            activeVisit,
            completedVisit2,
            completedVisit1
        )

        val response = handler.handle(memberId)

        response.visits.size shouldBe 3
        response.visits[0].id shouldBe activeVisit.id
        response.visits[0].isActive shouldBe true
        response.visits[1].id shouldBe completedVisit2.id
        response.visits[1].isActive shouldBe false
        response.visits[2].id shouldBe completedVisit1.id
        response.visits[2].isActive shouldBe false
    }

    test("Given member with active visit When get history Then should include it with active status") {
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val activeVisit = VisitBuilder.anActiveVisit(member = member)

        every { memberRepository.existsById(memberId) } returns true
        every { visitRepository.findByMemberIdOrderByCheckInTimeDesc(memberId) } returns listOf(activeVisit)

        val response = handler.handle(memberId)

        response.visits.size shouldBe 1
        response.visits[0].id shouldBe activeVisit.id
        response.visits[0].isActive shouldBe true
        response.visits[0].checkOutTime shouldBe null
    }

    test("Given member with no visits When get history Then should return empty list") {
        val memberId = UUID.randomUUID()

        every { memberRepository.existsById(memberId) } returns true
        every { visitRepository.findByMemberIdOrderByCheckInTimeDesc(memberId) } returns emptyList()

        val response = handler.handle(memberId)

        response.visits.size shouldBe 0
    }

    test("Given non-existent member When get history Then should throw ResourceNotFoundException") {
        val memberId = UUID.randomUUID()

        every { memberRepository.existsById(memberId) } returns false

        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(memberId)
        }

        exception.message shouldBe "Member not found: $memberId"
    }
})
