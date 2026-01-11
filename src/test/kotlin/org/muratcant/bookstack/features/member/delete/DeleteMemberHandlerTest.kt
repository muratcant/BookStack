package org.muratcant.bookstack.features.member.delete

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class DeleteMemberHandlerTest : FunSpec({

    val memberRepository = mockk<MemberRepository>()
    val handler = DeleteMemberHandler(memberRepository)

    beforeTest {
        clearMocks(memberRepository)
    }

    test("Given existing member id When delete member Then should delete member successfully") {
        // Given
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.aMember(id = memberId)
        
        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { memberRepository.delete(member) } returns Unit

        // When
        handler.handle(memberId)

        // Then
        verify(exactly = 1) { memberRepository.findById(memberId) }
        verify(exactly = 1) { memberRepository.delete(member) }
    }

    test("Given non-existing member id When delete member Then should throw ResourceNotFoundException") {
        // Given
        val memberId = UUID.randomUUID()
        every { memberRepository.findById(memberId) } returns Optional.empty()

        // When & Then
        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(memberId)
        }
        
        exception.message shouldBe "Member not found with id: $memberId"
        verify(exactly = 0) { memberRepository.delete(any()) }
    }
})

