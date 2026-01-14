package org.muratcant.bookstack.features.visit.checkin

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.muratcant.bookstack.shared.exception.MemberAlreadyCheckedInException
import org.muratcant.bookstack.shared.exception.MemberNotActiveException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class CheckInHandlerTest : FunSpec({

    val visitRepository = mockk<VisitRepository>()
    val memberRepository = mockk<MemberRepository>()
    val handler = CheckInHandler(visitRepository, memberRepository)

    beforeTest {
        clearMocks(visitRepository, memberRepository)
    }

    test("Given active member not checked in When check-in Then should create visit") {
        // Given
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId, firstName = "John", lastName = "Doe")
        val request = CheckInRequest(memberId = memberId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { visitRepository.existsByMemberIdAndCheckOutTimeIsNull(memberId) } returns false
        every { visitRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(request)

        // Then
        response.id shouldNotBe null
        response.memberId shouldBe memberId
        response.memberName shouldBe "John Doe"
        response.checkInTime shouldNotBe null
        verify(exactly = 1) { visitRepository.save(any()) }
    }

    test("Given suspended member When check-in Then should throw MemberNotActiveException") {
        // Given
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.aSuspendedMember(id = memberId)
        val request = CheckInRequest(memberId = memberId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)

        // When & Then
        val exception = shouldThrow<MemberNotActiveException> {
            handler.handle(request)
        }

        exception.message shouldBe "Member is not active: SUSPENDED"
        verify(exactly = 0) { visitRepository.save(any()) }
    }

    test("Given expired member When check-in Then should throw MemberNotActiveException") {
        // Given
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.anExpiredMember(id = memberId)
        val request = CheckInRequest(memberId = memberId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)

        // When & Then
        val exception = shouldThrow<MemberNotActiveException> {
            handler.handle(request)
        }

        exception.message shouldBe "Member is not active: EXPIRED"
        verify(exactly = 0) { visitRepository.save(any()) }
    }

    test("Given member already checked in When check-in Then should throw MemberAlreadyCheckedInException") {
        // Given
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val request = CheckInRequest(memberId = memberId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { visitRepository.existsByMemberIdAndCheckOutTimeIsNull(memberId) } returns true

        // When & Then
        val exception = shouldThrow<MemberAlreadyCheckedInException> {
            handler.handle(request)
        }

        exception.message shouldBe "Member is already checked in"
        verify(exactly = 0) { visitRepository.save(any()) }
    }

    test("Given non-existing member When check-in Then should throw ResourceNotFoundException") {
        // Given
        val memberId = UUID.randomUUID()
        val request = CheckInRequest(memberId = memberId)

        every { memberRepository.findById(memberId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(request)
        }

        exception.message shouldBe "Member not found: $memberId"
        verify(exactly = 0) { visitRepository.save(any()) }
    }
})
