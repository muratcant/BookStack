package org.muratcant.bookstack.features.member.get

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.domain.MemberStatus
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class GetMemberHandlerTest : FunSpec({

    val memberRepository = mockk<MemberRepository>()
    val handler = GetMemberHandler(memberRepository)

    beforeTest {
        clearMocks(memberRepository)
    }

    test("Given existing member id When get member Then should return member") {
        // Given
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.aMember(
            id = memberId,
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com"
        )
        
        every { memberRepository.findById(memberId) } returns Optional.of(member)

        // When
        val response = handler.handle(memberId)

        // Then
        response.id shouldBe memberId
        response.firstName shouldBe "John"
        response.lastName shouldBe "Doe"
        response.email shouldBe "john@example.com"
        response.status shouldBe "ACTIVE"
    }

    test("Given non-existing member id When get member Then should throw ResourceNotFoundException") {
        // Given
        val memberId = UUID.randomUUID()
        every { memberRepository.findById(memberId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(memberId)
        }
        
        exception.message shouldBe "Member not found with id: $memberId"
    }

    test("Given member with SUSPENDED status When get member Then should return SUSPENDED status") {
        // Given
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.aSuspendedMember(
            id = memberId,
            firstName = "Suspended",
            lastName = "User",
            email = "suspended@example.com"
        )
        
        every { memberRepository.findById(memberId) } returns Optional.of(member)

        // When
        val response = handler.handle(memberId)

        // Then
        response.id shouldBe memberId
        response.status shouldBe "SUSPENDED"
        response.firstName shouldBe "Suspended"
    }

    test("Given member with EXPIRED status When get member Then should return EXPIRED status") {
        // Given
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.anExpiredMember(
            id = memberId,
            firstName = "Expired",
            lastName = "User",
            email = "expired@example.com"
        )
        
        every { memberRepository.findById(memberId) } returns Optional.of(member)

        // When
        val response = handler.handle(memberId)

        // Then
        response.id shouldBe memberId
        response.status shouldBe "EXPIRED"
        response.firstName shouldBe "Expired"
    }

    test("Given member with null phone When get member Then should return null phone") {
        // Given
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.aMember(
            id = memberId,
            firstName = "NoPhone",
            lastName = "User",
            email = "nophone@example.com",
            phone = null
        )
        
        every { memberRepository.findById(memberId) } returns Optional.of(member)

        // When
        val response = handler.handle(memberId)

        // Then
        response.id shouldBe memberId
        response.phone shouldBe null
    }

    test("Given member with maxActiveLoans When get member Then should return correct maxActiveLoans") {
        // Given
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.aMember(
            id = memberId,
            firstName = "MaxLoans",
            lastName = "User",
            email = "maxloans@example.com",
            maxActiveLoans = 10
        )
        
        every { memberRepository.findById(memberId) } returns Optional.of(member)

        // When
        val response = handler.handle(memberId)

        // Then
        response.id shouldBe memberId
        response.maxActiveLoans shouldBe 10
    }
})

