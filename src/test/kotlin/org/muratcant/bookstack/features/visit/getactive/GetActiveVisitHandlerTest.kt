package org.muratcant.bookstack.features.visit.getactive

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.muratcant.bookstack.features.visit.test.VisitBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.UUID

class GetActiveVisitHandlerTest : FunSpec({

    val visitRepository = mockk<VisitRepository>()
    val memberRepository = mockk<MemberRepository>()
    val handler = GetActiveVisitHandler(visitRepository, memberRepository)

    beforeTest {
        clearMocks(visitRepository, memberRepository)
    }

    test("Given member with active visit When get active Then should return visit details") {
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val activeVisit = VisitBuilder.anActiveVisit(member = member)

        every { memberRepository.existsById(memberId) } returns true
        every { visitRepository.findByMemberIdAndCheckOutTimeIsNull(memberId) } returns activeVisit

        val response = handler.handle(memberId)

        response shouldNotBe null
        response?.id shouldBe activeVisit.id
        response?.memberId shouldBe memberId
        response?.memberName shouldBe "${member.firstName} ${member.lastName}"
        response?.checkInTime shouldNotBe null
    }

    test("Given member with no active visit When get active Then should return null") {
        val memberId = UUID.randomUUID()

        every { memberRepository.existsById(memberId) } returns true
        every { visitRepository.findByMemberIdAndCheckOutTimeIsNull(memberId) } returns null

        val response = handler.handle(memberId)

        response shouldBe null
    }

    test("Given non-existent member When get active visit Then should throw ResourceNotFoundException") {
        val memberId = UUID.randomUUID()

        every { memberRepository.existsById(memberId) } returns false

        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(memberId)
        }

        exception.message shouldBe "Member not found: $memberId"
    }
})
