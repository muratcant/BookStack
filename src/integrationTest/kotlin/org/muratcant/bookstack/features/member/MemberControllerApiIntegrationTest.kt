package org.muratcant.bookstack.features.member

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.muratcant.bookstack.BaseIntegrationTest
import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.domain.MemberStatus
import org.muratcant.bookstack.features.member.register.RegisterMemberRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MemberControllerApiIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @BeforeEach
    fun setup() {
        memberRepository.deleteAll()
    }

    // ==================== POST /api/members ====================

    @Test
    fun `Given valid request When POST api members Then should return 201 and save to database`() {
        // Given
        val request = RegisterMemberRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "+90 555 123 4567"
        )

        // When & Then
        mockMvc.perform(
            post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.phone").value("+90 555 123 4567"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.membershipNumber").exists())

        // Verify DB
        val savedMember = memberRepository.findByEmail("john.doe@example.com")
        assertNotNull(savedMember)
        assertEquals("John", savedMember.firstName)
        assertEquals(MemberStatus.ACTIVE, savedMember.status)
    }

    @Test
    fun `Given request without phone When POST api members Then should return 201 with null phone`() {
        // Given
        val request = RegisterMemberRequest(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@example.com",
            phone = null
        )

        // When & Then
        mockMvc.perform(
            post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.firstName").value("Jane"))
            .andExpect(jsonPath("$.phone").doesNotExist())

        // Verify DB
        val savedMember = memberRepository.findByEmail("jane.smith@example.com")
        assertNotNull(savedMember)
        assertNull(savedMember.phone)
    }

    @Test
    fun `Given duplicate email When POST api members Then should return 400`() {
        // Given - Önce bir üye kaydet
        memberRepository.save(
            Member(
                membershipNumber = "MBR-12345678",
                firstName = "Existing",
                lastName = "Member",
                email = "duplicate@example.com"
            )
        )

        val request = RegisterMemberRequest(
            firstName = "New",
            lastName = "Member",
            email = "duplicate@example.com",
            phone = null
        )

        // When & Then
        mockMvc.perform(
            post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `Given blank firstName When POST api members Then should return 400`() {
        // Given
        val request = mapOf(
            "firstName" to "",
            "lastName" to "Doe",
            "email" to "test@example.com"
        )

        // When & Then
        mockMvc.perform(
            post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    // ==================== GET /api/members/{id} ====================

    @Test
    fun `Given existing member When GET api members id Then should return 200`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-ABCD1234",
                firstName = "John",
                lastName = "Doe",
                email = "john@example.com"
            )
        )

        // When & Then
        mockMvc.perform(get("/api/members/${member.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(member.id.toString()))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.email").value("john@example.com"))
    }

    @Test
    fun `Given non-existing member When GET api members id Then should return 404`() {
        // When & Then
        mockMvc.perform(get("/api/members/${UUID.randomUUID()}"))
            .andExpect(status().isNotFound)
    }

    // ==================== GET /api/members ====================

    @Test
    fun `Given multiple members When GET api members Then should return all`() {
        // Given
        memberRepository.save(Member(
            membershipNumber = "MBR-11111111",
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com"
        ))
        memberRepository.save(Member(
            membershipNumber = "MBR-22222222",
            firstName = "Jane",
            lastName = "Smith",
            email = "jane@example.com"
        ))

        // When & Then
        mockMvc.perform(get("/api/members"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.members.length()").value(2))
    }

    @Test
    fun `Given no members When GET api members Then should return empty list`() {
        // When & Then
        mockMvc.perform(get("/api/members"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.members.length()").value(0))
    }

    // ==================== PUT /api/members/{id} ====================

    @Test
    fun `Given valid update When PUT api members id Then should update in database`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-UPDATE01",
                firstName = "John",
                lastName = "Doe",
                email = "john@example.com"
            )
        )

        val updateRequest = mapOf(
            "firstName" to "Johnny",
            "lastName" to "Updated",
            "email" to "johnny@example.com",
            "phone" to "+90 555 999 8888"
        )

        // When & Then
        mockMvc.perform(
            put("/api/members/${member.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.firstName").value("Johnny"))
            .andExpect(jsonPath("$.lastName").value("Updated"))
            .andExpect(jsonPath("$.email").value("johnny@example.com"))
            .andExpect(jsonPath("$.phone").value("+90 555 999 8888"))

        // Verify DB
        val updated = memberRepository.findById(member.id).orElse(null)
        assertNotNull(updated)
        assertEquals("Johnny", updated.firstName)
        assertEquals("johnny@example.com", updated.email)
    }

    @Test
    fun `Given non-existing member When PUT api members id Then should return 404`() {
        // Given
        val updateRequest = mapOf(
            "firstName" to "Test",
            "lastName" to "Test",
            "email" to "test@example.com"
        )

        // When & Then
        mockMvc.perform(
            put("/api/members/${UUID.randomUUID()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Given duplicate email in update When PUT api members id Then should return 400`() {
        // Given - İki üye oluştur
        val member1 = memberRepository.save(Member(
            membershipNumber = "MBR-MEMBER01",
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com"
        ))
        memberRepository.save(Member(
            membershipNumber = "MBR-MEMBER02",
            firstName = "Jane",
            lastName = "Smith",
            email = "jane@example.com"
        ))

        val updateRequest = mapOf(
            "firstName" to "John",
            "lastName" to "Doe",
            "email" to "jane@example.com"
        )

        // When & Then
        mockMvc.perform(
            put("/api/members/${member1.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
    }

    // ==================== DELETE /api/members/{id} ====================

    @Test
    fun `Given existing member When DELETE api members id Then should remove from database`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-DELETE01",
                firstName = "ToDelete",
                lastName = "Member",
                email = "delete@example.com"
            )
        )

        // When & Then
        mockMvc.perform(delete("/api/members/${member.id}"))
            .andExpect(status().isNoContent)

        // Verify DB
        val deleted = memberRepository.findById(member.id).orElse(null)
        assertNull(deleted)
    }

    @Test
    fun `Given non-existing member When DELETE api members id Then should return 404`() {
        // When & Then
        mockMvc.perform(delete("/api/members/${UUID.randomUUID()}"))
            .andExpect(status().isNotFound)
    }
}
