package org.muratcant.bookstack.features.member.activate

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

class ActivateMemberHandlerTest : FunSpec({

    val memberRepository = mockk<MemberRepository>()
    val handler = ActivateMemberHandler(memberRepository)

    beforeTest {
        clearMocks(memberRepository)
    }

    test("Given suspended member When activate Then should change status to ACTIVE") {
        // Given
        val memberId = UUID.randomUUID()
        val suspendedMember = MemberBuilder.aSuspendedMember(id = memberId)
        
        every { memberRepository.findById(memberId) } returns Optional.of(suspendedMember)
        every { memberRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(memberId)

        // Then
        response.id shouldBe memberId
        response.status shouldBe "ACTIVE"
        verify(exactly = 1) { memberRepository.save(any()) }
    }

    test("Given expired member When activate Then should change status to ACTIVE") {
        // Given
        val memberId = UUID.randomUUID()
        val expiredMember = MemberBuilder.anExpiredMember(id = memberId)
        
        every { memberRepository.findById(memberId) } returns Optional.of(expiredMember)
        every { memberRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(memberId)

        // Then
        response.id shouldBe memberId
        response.status shouldBe "ACTIVE"
        verify(exactly = 1) { memberRepository.save(any()) }
    }

    test("Given active member When activate Then should throw InvalidStatusTransitionException") {
        // Given
        val memberId = UUID.randomUUID()
        val activeMember = MemberBuilder.anActiveMember(id = memberId)
        
        every { memberRepository.findById(memberId) } returns Optional.of(activeMember)

        // When & Then
        val exception = shouldThrow<InvalidStatusTransitionException> {
            handler.handle(memberId)
        }
        
        exception.message shouldBe "Member is already active"
        verify(exactly = 0) { memberRepository.save(any()) }
    }

    test("Given non-existing member id When activate Then should throw ResourceNotFoundException") {
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

    test("Given suspended member When activate Then should preserve other member fields") {
        // Given
        val memberId = UUID.randomUUID()
        val suspendedMember = MemberBuilder.aSuspendedMember(
            id = memberId,
            firstName = "Jane",
            lastName = "Doe",
            email = "jane@example.com"
        )
        
        every { memberRepository.findById(memberId) } returns Optional.of(suspendedMember)
        every { memberRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(memberId)

        // Then
        response.firstName shouldBe "Jane"
        response.lastName shouldBe "Doe"
        response.email shouldBe "jane@example.com"
        response.membershipNumber shouldBe suspendedMember.membershipNumber
    }

    test("Given expired member When activate Then should preserve other member fields") {
        // Given
        val memberId = UUID.randomUUID()
        val expiredMember = MemberBuilder.anExpiredMember(
            id = memberId,
            firstName = "Bob",
            lastName = "Smith",
            email = "bob@example.com"
        )
        
        every { memberRepository.findById(memberId) } returns Optional.of(expiredMember)
        every { memberRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(memberId)

        // Then
        response.firstName shouldBe "Bob"
        response.lastName shouldBe "Smith"
        response.email shouldBe "bob@example.com"
        response.membershipNumber shouldBe expiredMember.membershipNumber
        response.status shouldBe "ACTIVE"
    }
})

