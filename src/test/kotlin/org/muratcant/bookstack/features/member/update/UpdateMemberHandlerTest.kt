package org.muratcant.bookstack.features.member.update

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.shared.exception.DuplicateResourceException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class UpdateMemberHandlerTest : FunSpec({

    val memberRepository = mockk<MemberRepository>()
    val handler = UpdateMemberHandler(memberRepository)

    beforeTest {
        clearMocks(memberRepository)
    }

    test("Given existing member id and valid request When update member Then should update member successfully") {
        // Given
        val memberId = UUID.randomUUID()
        val existingMember = MemberBuilder.aMember(
            id = memberId,
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com"
        )
        val request = UpdateMemberRequest(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane@example.com",
            phone = "+90 555 999 8888"
        )
        
        every { memberRepository.findById(memberId) } returns Optional.of(existingMember)
        every { memberRepository.findByEmail(request.email) } returns null
        every { memberRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(memberId, request)

        // Then
        response.id shouldBe memberId
        response.firstName shouldBe "Jane"
        response.lastName shouldBe "Smith"
        response.email shouldBe "jane@example.com"
        response.phone shouldBe "+90 555 999 8888"
        verify(exactly = 1) { memberRepository.save(any()) }
    }

    test("Given non-existing member id When update member Then should throw ResourceNotFoundException") {
        // Given
        val memberId = UUID.randomUUID()
        val request = UpdateMemberRequest(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane@example.com",
            phone = null
        )
        
        every { memberRepository.findById(memberId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(memberId, request)
        }
        
        exception.message shouldBe "Member not found with id: $memberId"
        verify(exactly = 0) { memberRepository.save(any()) }
    }

    test("Given duplicate email from another member When update member Then should throw DuplicateResourceException") {
        // Given
        val memberId = UUID.randomUUID()
        val existingMember = MemberBuilder.aMember(id = memberId, email = "john@example.com")
        val otherMember = MemberBuilder.aMember(email = "existing@example.com")
        val request = UpdateMemberRequest(
            firstName = "John",
            lastName = "Doe",
            email = "existing@example.com",
            phone = null
        )
        
        every { memberRepository.findById(memberId) } returns Optional.of(existingMember)
        every { memberRepository.findByEmail(request.email) } returns otherMember

        // When & Then
        val exception = shouldThrow<DuplicateResourceException> {
            handler.handle(memberId, request)
        }
        
        exception.message shouldBe "Email already exists: ${request.email}"
        verify(exactly = 0) { memberRepository.save(any()) }
    }

    test("Given same email When update member Then should allow update") {
        // Given
        val memberId = UUID.randomUUID()
        val existingMember = MemberBuilder.aMember(
            id = memberId,
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com"
        )
        val request = UpdateMemberRequest(
            firstName = "John",
            lastName = "Doe Updated",
            email = "john@example.com",
            phone = null
        )
        
        every { memberRepository.findById(memberId) } returns Optional.of(existingMember)
        every { memberRepository.findByEmail(request.email) } returns existingMember
        every { memberRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(memberId, request)

        // Then
        response.id shouldBe memberId
        response.email shouldBe "john@example.com"
        response.lastName shouldBe "Doe Updated"
        verify(exactly = 1) { memberRepository.save(any()) }
    }

    test("Given member with phone When update phone to null Then should update phone to null") {
        // Given
        val memberId = UUID.randomUUID()
        val existingMember = MemberBuilder.aMember(
            id = memberId,
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            phone = "+90 555 123 4567"
        )
        val request = UpdateMemberRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            phone = null
        )
        
        every { memberRepository.findById(memberId) } returns Optional.of(existingMember)
        every { memberRepository.findByEmail(request.email) } returns existingMember
        every { memberRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(memberId, request)

        // Then
        response.id shouldBe memberId
        response.phone shouldBe null
        verify(exactly = 1) { memberRepository.save(any()) }
    }

    test("Given member with null phone When update phone to value Then should update phone") {
        // Given
        val memberId = UUID.randomUUID()
        val existingMember = MemberBuilder.aMember(
            id = memberId,
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            phone = null
        )
        val request = UpdateMemberRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            phone = "+90 555 999 8888"
        )
        
        every { memberRepository.findById(memberId) } returns Optional.of(existingMember)
        every { memberRepository.findByEmail(request.email) } returns existingMember
        every { memberRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(memberId, request)

        // Then
        response.id shouldBe memberId
        response.phone shouldBe "+90 555 999 8888"
        verify(exactly = 1) { memberRepository.save(any()) }
    }

    test("Given member When update all fields Then should update all fields correctly") {
        // Given
        val memberId = UUID.randomUUID()
        val existingMember = MemberBuilder.aMember(
            id = memberId,
            firstName = "Old",
            lastName = "Name",
            email = "old@example.com",
            phone = "+90 555 111 1111"
        )
        val request = UpdateMemberRequest(
            firstName = "New",
            lastName = "Name",
            email = "new@example.com",
            phone = "+90 555 222 2222"
        )
        
        every { memberRepository.findById(memberId) } returns Optional.of(existingMember)
        every { memberRepository.findByEmail(request.email) } returns null
        every { memberRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(memberId, request)

        // Then
        response.id shouldBe memberId
        response.firstName shouldBe "New"
        response.lastName shouldBe "Name"
        response.email shouldBe "new@example.com"
        response.phone shouldBe "+90 555 222 2222"
        verify(exactly = 1) { memberRepository.save(any()) }
    }
})

