package org.muratcant.bookstack.features.visit

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.muratcant.bookstack.BaseIntegrationTest
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.domain.MemberStatus
import org.muratcant.bookstack.features.visit.domain.Visit
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Transactional
class VisitControllerApiIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var visitRepository: VisitRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var loanRepository: LoanRepository

    @BeforeEach
    fun setup() {
        loanRepository.deleteAll()
        visitRepository.deleteAll()
        memberRepository.deleteAll()
    }

    // ==================== POST /api/visits/checkin ====================

    @Test
    fun `Given active member not checked in When POST api visits checkin Then should return 201`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-CHECKIN01",
                firstName = "John",
                lastName = "Doe",
                email = "john@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val request = mapOf("memberId" to member.id.toString())

        // When & Then
        mockMvc.perform(
            post("/api/visits/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.memberId").value(member.id.toString()))
            .andExpect(jsonPath("$.memberName").value("John Doe"))
            .andExpect(jsonPath("$.checkInTime").exists())

        // Verify DB
        val activeVisit = visitRepository.findByMemberIdAndCheckOutTimeIsNull(member.id)
        assertNotNull(activeVisit)
    }

    @Test
    fun `Given suspended member When POST api visits checkin Then should return 400`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-SUSPEND01",
                firstName = "Suspended",
                lastName = "Member",
                email = "suspended@example.com",
                status = MemberStatus.SUSPENDED
            )
        )

        val request = mapOf("memberId" to member.id.toString())

        // When & Then
        mockMvc.perform(
            post("/api/visits/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Member is not active: SUSPENDED"))
    }

    @Test
    fun `Given expired member When POST api visits checkin Then should return 400`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-EXPIRED01",
                firstName = "Expired",
                lastName = "Member",
                email = "expired@example.com",
                status = MemberStatus.EXPIRED
            )
        )

        val request = mapOf("memberId" to member.id.toString())

        // When & Then
        mockMvc.perform(
            post("/api/visits/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Member is not active: EXPIRED"))
    }

    @Test
    fun `Given member already checked in When POST api visits checkin Then should return 400`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-ALREADY01",
                firstName = "Already",
                lastName = "Inside",
                email = "already@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        visitRepository.save(Visit(member = member))

        val request = mapOf("memberId" to member.id.toString())

        // When & Then
        mockMvc.perform(
            post("/api/visits/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Member is already checked in"))
    }

    @Test
    fun `Given non-existing member When POST api visits checkin Then should return 404`() {
        // Given
        val request = mapOf("memberId" to UUID.randomUUID().toString())

        // When & Then
        mockMvc.perform(
            post("/api/visits/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
    }

    // ==================== POST /api/visits/{id}/checkout ====================

    @Test
    fun `Given active visit When POST api visits id checkout Then should return 200`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-CHECKOUT1",
                firstName = "John",
                lastName = "Doe",
                email = "checkout@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val visit = visitRepository.save(Visit(member = member))

        // When & Then
        mockMvc.perform(post("/api/visits/${visit.id}/checkout"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(visit.id.toString()))
            .andExpect(jsonPath("$.memberId").value(member.id.toString()))
            .andExpect(jsonPath("$.memberName").value("John Doe"))
            .andExpect(jsonPath("$.checkInTime").exists())
            .andExpect(jsonPath("$.checkOutTime").exists())

        // Verify DB
        val checkedOut = visitRepository.findById(visit.id).orElse(null)
        assertNotNull(checkedOut)
        assertNotNull(checkedOut.checkOutTime)
    }

    @Test
    fun `Given already checked out visit When POST api visits id checkout Then should return 400`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-ALREADYCO",
                firstName = "Already",
                lastName = "Out",
                email = "alreadyout@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val visit = visitRepository.save(
            Visit(
                member = member,
                checkInTime = LocalDateTime.now().minusHours(2),
                checkOutTime = LocalDateTime.now()
            )
        )

        // When & Then
        mockMvc.perform(post("/api/visits/${visit.id}/checkout"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Visit is already checked out"))
    }

    @Test
    fun `Given non-existing visit When POST api visits id checkout Then should return 404`() {
        // When & Then
        mockMvc.perform(post("/api/visits/${UUID.randomUUID()}/checkout"))
            .andExpect(status().isNotFound)
    }

    // ==================== GET /api/visits/{id} ====================

    @Test
    fun `Given existing visit When GET api visits id Then should return 200`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-GETVISIT1",
                firstName = "John",
                lastName = "Doe",
                email = "getvisit@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val visit = visitRepository.save(Visit(member = member))

        // When & Then
        mockMvc.perform(get("/api/visits/${visit.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(visit.id.toString()))
            .andExpect(jsonPath("$.memberId").value(member.id.toString()))
            .andExpect(jsonPath("$.memberName").value("John Doe"))
            .andExpect(jsonPath("$.membershipNumber").value("MBR-GETVISIT1"))
            .andExpect(jsonPath("$.active").value(true))
    }

    @Test
    fun `Given non-existing visit When GET api visits id Then should return 404`() {
        // When & Then
        mockMvc.perform(get("/api/visits/${UUID.randomUUID()}"))
            .andExpect(status().isNotFound)
    }

    // ==================== GET /api/members/{memberId}/visits/active ====================

    @Test
    fun `Given member with active visit When GET api members memberId visits active Then should return visit`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-ACTIVE01",
                firstName = "Active",
                lastName = "Visitor",
                email = "activevisit@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val visit = visitRepository.save(Visit(member = member))

        // When & Then
        mockMvc.perform(get("/api/members/${member.id}/visits/active"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(visit.id.toString()))
            .andExpect(jsonPath("$.memberId").value(member.id.toString()))
            .andExpect(jsonPath("$.memberName").value("Active Visitor"))
    }

    @Test
    fun `Given member without active visit When GET api members memberId visits active Then should return null`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-NOACTIVE1",
                firstName = "No",
                lastName = "Visit",
                email = "novisit@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        // When & Then
        mockMvc.perform(get("/api/members/${member.id}/visits/active"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").doesNotExist())
    }

    @Test
    fun `Given non-existing member When GET api members memberId visits active Then should return 404`() {
        // When & Then
        mockMvc.perform(get("/api/members/${UUID.randomUUID()}/visits/active"))
            .andExpect(status().isNotFound)
    }

    // ==================== GET /api/members/{memberId}/visits ====================

    @Test
    fun `Given member with visit history When GET api members memberId visits Then should return all visits`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-HISTORY01",
                firstName = "History",
                lastName = "Member",
                email = "history@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        // Tamamlanmış ziyaret
        visitRepository.save(
            Visit(
                member = member,
                checkInTime = LocalDateTime.now().minusDays(1),
                checkOutTime = LocalDateTime.now().minusDays(1).plusHours(2)
            )
        )

        // Aktif ziyaret
        visitRepository.save(Visit(member = member))

        // When & Then
        mockMvc.perform(get("/api/members/${member.id}/visits"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.visits.length()").value(2))
    }

    @Test
    fun `Given member without visits When GET api members memberId visits Then should return empty list`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-NOHISTORY",
                firstName = "No",
                lastName = "History",
                email = "nohistory@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        // When & Then
        mockMvc.perform(get("/api/members/${member.id}/visits"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.visits.length()").value(0))
    }

    @Test
    fun `Given non-existing member When GET api members memberId visits Then should return 404`() {
        // When & Then
        mockMvc.perform(get("/api/members/${UUID.randomUUID()}/visits"))
            .andExpect(status().isNotFound)
    }
}
