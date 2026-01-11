package org.muratcant.bookstack.features.member.list

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.domain.MemberStatus
import org.muratcant.bookstack.features.member.test.MemberBuilder

class ListMembersHandlerTest : FunSpec({

    val memberRepository = mockk<MemberRepository>()
    val handler = ListMembersHandler(memberRepository)

    beforeTest {
        clearMocks(memberRepository)
    }

    test("Given members exist When list members Then should return all members") {
        // Given
        val member1 = MemberBuilder.aMember(firstName = "John", lastName = "Doe", email = "john@example.com")
        val member2 = MemberBuilder.aMember(firstName = "Jane", lastName = "Smith", email = "jane@example.com")
        val members = listOf(member1, member2)
        
        every { memberRepository.findAll() } returns members

        // When
        val response = handler.handle()

        // Then
        response.members shouldHaveSize 2
        response.members[0].fullName shouldBe "John Doe"
        response.members[0].email shouldBe "john@example.com"
        response.members[1].fullName shouldBe "Jane Smith"
        response.members[1].email shouldBe "jane@example.com"
    }

    test("Given no members exist When list members Then should return empty list") {
        // Given
        every { memberRepository.findAll() } returns emptyList()

        // When
        val response = handler.handle()

        // Then
        response.members shouldHaveSize 0
    }

    test("Given members with different statuses When list members Then should return all with correct statuses") {
        // Given
        val member1 = MemberBuilder.anActiveMember(firstName = "Active", lastName = "User", email = "active@example.com")
        val member2 = MemberBuilder.aSuspendedMember(firstName = "Suspended", lastName = "User", email = "suspended@example.com")
        val member3 = MemberBuilder.anExpiredMember(firstName = "Expired", lastName = "User", email = "expired@example.com")
        val members = listOf(member1, member2, member3)
        
        every { memberRepository.findAll() } returns members

        // When
        val response = handler.handle()

        // Then
        response.members shouldHaveSize 3
        response.members[0].status shouldBe "ACTIVE"
        response.members[0].fullName shouldBe "Active User"
        response.members[1].status shouldBe "SUSPENDED"
        response.members[1].fullName shouldBe "Suspended User"
        response.members[2].status shouldBe "EXPIRED"
        response.members[2].fullName shouldBe "Expired User"
    }

    test("Given single member When list members Then should return list with one member") {
        // Given
        val member = MemberBuilder.aMember(firstName = "Single", lastName = "Member", email = "single@example.com")
        every { memberRepository.findAll() } returns listOf(member)

        // When
        val response = handler.handle()

        // Then
        response.members shouldHaveSize 1
        response.members[0].fullName shouldBe "Single Member"
        response.members[0].email shouldBe "single@example.com"
        response.members[0].membershipNumber shouldBe member.membershipNumber
    }

    test("Given member with null phone When list members Then should handle correctly") {
        // Given
        val member = MemberBuilder.aMember(
            firstName = "NoPhone",
            lastName = "User",
            email = "nophone@example.com",
            phone = null
        )
        every { memberRepository.findAll() } returns listOf(member)

        // When
        val response = handler.handle()

        // Then
        response.members shouldHaveSize 1
        response.members[0].fullName shouldBe "NoPhone User"
        response.members[0].email shouldBe "nophone@example.com"
    }
})

