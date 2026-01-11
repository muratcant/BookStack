package org.muratcant.bookstack.features.member.register

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.shared.exception.DuplicateResourceException

class RegisterMemberHandlerTest : FunSpec({

    val memberRepository = mockk<MemberRepository>()
    val handler = RegisterMemberHandler(memberRepository)

    beforeTest {
        clearMocks(memberRepository)
    }

    test("Given valid member request When register Then should create member successfully") {
        // Given
        val request = RegisterMemberRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "+90 555 123 4567"
        )
        
        every { memberRepository.existsByEmail(request.email) } returns false
        every { memberRepository.save(any()) } answers { firstArg<Member>() }

        // When
        val response = handler.handle(request)

        // Then
        response.id shouldNotBe null
        response.firstName shouldBe "John"
        response.lastName shouldBe "Doe"
        response.email shouldBe "john.doe@example.com"
        response.phone shouldBe "+90 555 123 4567"
        response.status shouldBe "ACTIVE"
        response.membershipNumber shouldNotBe null
        verify(exactly = 1) { memberRepository.existsByEmail(request.email) }
        verify(exactly = 1) { memberRepository.save(any()) }
    }

    test("Given duplicate email When register Then should throw DuplicateResourceException") {
        // Given
        val request = RegisterMemberRequest(
            firstName = "John",
            lastName = "Doe",
            email = "existing@example.com",
            phone = null
        )
        
        every { memberRepository.existsByEmail(request.email) } returns true

        // When & Then
        val exception = shouldThrow<DuplicateResourceException> {
            handler.handle(request)
        }
        
        exception.message shouldBe "Email already exists: ${request.email}"
        verify(exactly = 1) { memberRepository.existsByEmail(request.email) }
        verify(exactly = 0) { memberRepository.save(any()) }
    }

    test("Given member without phone When register Then should create member with null phone") {
        // Given
        val request = RegisterMemberRequest(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@example.com",
            phone = null
        )
        
        every { memberRepository.existsByEmail(request.email) } returns false
        every { memberRepository.save(any()) } answers { firstArg<Member>() }

        // When
        val response = handler.handle(request)

        // Then
        response.id shouldNotBe null
        response.firstName shouldBe "Jane"
        response.lastName shouldBe "Smith"
        response.email shouldBe "jane.smith@example.com"
        response.phone shouldBe null
        response.status shouldBe "ACTIVE"
        verify(exactly = 1) { memberRepository.save(any()) }
    }

    test("Given valid request When register Then should generate unique membership number") {
        // Given
        val request = RegisterMemberRequest(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = null
        )
        
        every { memberRepository.existsByEmail(request.email) } returns false
        every { memberRepository.save(any()) } answers { firstArg<Member>() }

        // When
        val response = handler.handle(request)

        // Then
        response.membershipNumber shouldNotBe null
        response.membershipNumber.startsWith("MBR-") shouldBe true
        response.membershipNumber.length shouldBe 12 // MBR- + 8 chars
    }

    test("Given valid request When register Then should set status to ACTIVE") {
        // Given
        val request = RegisterMemberRequest(
            firstName = "New",
            lastName = "Member",
            email = "new@example.com",
            phone = "+90 555 111 2222"
        )
        
        every { memberRepository.existsByEmail(request.email) } returns false
        every { memberRepository.save(any()) } answers { firstArg<Member>() }

        // When
        val response = handler.handle(request)

        // Then
        response.status shouldBe "ACTIVE"
    }

    test("Given valid request When register Then membership number should have correct format") {
        // Given
        val request = RegisterMemberRequest(
            firstName = "Format",
            lastName = "Test",
            email = "format@example.com",
            phone = null
        )
        
        every { memberRepository.existsByEmail(request.email) } returns false
        every { memberRepository.save(any()) } answers { firstArg<Member>() }

        // When
        val response = handler.handle(request)

        // Then
        response.membershipNumber.matches(Regex("MBR-[A-Z0-9]{8}")) shouldBe true
    }
})

