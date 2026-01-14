package org.muratcant.bookstack.features.member.suspend

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.domain.MemberStatus
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.shared.exception.InvalidStatusTransitionException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class SuspendMemberHandlerTest : FunSpec({

    val memberRepository = mockk<MemberRepository>()
    val handler = SuspendMemberHandler(memberRepository)

    beforeTest {
        clearMocks(memberRepository)
    }

    test("Given active member When suspend Then should change status to SUSPENDED") {
        // Given
        val memberId = UUID.randomUUID()
        val activeMember = MemberBuilder.anActiveMember(id = memberId)
        
        every { memberRepository.findById(memberId) } returns Optional.of(activeMember)
        every { memberRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(memberId)

        // Then
        response.id shouldBe memberId
        response.status shouldBe "SUSPENDED"
        verify(exactly = 1) { memberRepository.save(any()) }
    }

    test("Given suspended member When suspend Then should throw InvalidStatusTransitionException") {
        // Given
        val memberId = UUID.randomUUID()
        val suspendedMember = MemberBuilder.aSuspendedMember(id = memberId)
        
        every { memberRepository.findById(memberId) } returns Optional.of(suspendedMember)

        // When & Then
        val exception = shouldThrow<InvalidStatusTransitionException> {
            handler.handle(memberId)
        }
        
        exception.message shouldBe "Member is already suspended"
        verify(exactly = 0) { memberRepository.save(any()) }
    }

    test("Given expired member When suspend Then should throw InvalidStatusTransitionException") {
        // Given
        val memberId = UUID.randomUUID()
        val expiredMember = MemberBuilder.anExpiredMember(id = memberId)
        
        every { memberRepository.findById(memberId) } returns Optional.of(expiredMember)

        // When & Then
        val exception = shouldThrow<InvalidStatusTransitionException> {
            handler.handle(memberId)
        }
        
        exception.message shouldBe "Cannot suspend an expired member. Please activate first."
        verify(exactly = 0) { memberRepository.save(any()) }
    }

    test("Given non-existing member id When suspend Then should throw ResourceNotFoundException") {
        // Given
        val memberId = UUID.randomUUID()
        
        every { memberRepository.findById(memberId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(memberId)
        }
        
        exception.message shouldBe "Member not found with id: $memberId"
        verify(exactly = 0) { memberRepository.save(any()) }
    }

    test("Given active member When suspend Then should preserve other member fields") {
        // Given
        val memberId = UUID.randomUUID()
        val activeMember = MemberBuilder.anActiveMember(
            id = memberId,
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com"
        )
        
        every { memberRepository.findById(memberId) } returns Optional.of(activeMember)
        every { memberRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(memberId)

        // Then
        response.firstName shouldBe "John"
        response.lastName shouldBe "Doe"
        response.email shouldBe "john@example.com"
        response.membershipNumber shouldBe activeMember.membershipNumber
    }
})

