package org.muratcant.bookstack.features.loan

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.muratcant.bookstack.BaseIntegrationTest
import org.muratcant.bookstack.features.book.domain.Book
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.bookcopy.domain.BookCopy
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.bookcopy.domain.UsageType
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
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Transactional
class LoanControllerApiIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var loanRepository: LoanRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var bookCopyRepository: BookCopyRepository

    @Autowired
    private lateinit var visitRepository: VisitRepository

    @BeforeEach
    fun setup() {
        loanRepository.deleteAll()
        visitRepository.deleteAll()
        bookCopyRepository.deleteAll()
        bookRepository.deleteAll()
        memberRepository.deleteAll()
    }

    // ==================== POST /api/loans ====================

    @Test
    fun `Given active checked-in member and available copy When POST api loans Then should return 201`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-LOAN01",
                firstName = "John",
                lastName = "Doe",
                email = "john@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val book = bookRepository.save(
            Book(
                isbn = "978-0-13-235088-4",
                title = "Clean Code"
            )
        )

        val copy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-LOAN-001",
                usageType = UsageType.BOTH,
                status = CopyStatus.AVAILABLE
            )
        )

        visitRepository.save(Visit(member = member))

        val request = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to copy.id.toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.memberId").value(member.id.toString()))
            .andExpect(jsonPath("$.copyId").value(copy.id.toString()))
            .andExpect(jsonPath("$.status").value("ACTIVE"))

        // Verify DB
        val savedCopy = bookCopyRepository.findById(copy.id).orElse(null)
        assertNotNull(savedCopy)
        assertEquals(CopyStatus.LOANED, savedCopy.status)
    }

    @Test
    fun `Given suspended member When POST api loans Then should return 400`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-SUSPEND1",
                firstName = "Suspended",
                lastName = "Member",
                email = "suspended@example.com",
                status = MemberStatus.SUSPENDED
            )
        )

        val book = bookRepository.save(
            Book(
                isbn = "978-0-13-235088-5",
                title = "Clean Architecture"
            )
        )

        val copy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-LOAN-002",
                usageType = UsageType.BOTH,
                status = CopyStatus.AVAILABLE
            )
        )

        val request = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to copy.id.toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Member is not active: SUSPENDED"))
    }

    @Test
    fun `Given member not checked in When POST api loans Then should return 400`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-NOTCHECKED",
                firstName = "Not",
                lastName = "CheckedIn",
                email = "notchecked@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val book = bookRepository.save(
            Book(
                isbn = "978-0-13-235088-6",
                title = "Test Book"
            )
        )

        val copy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-LOAN-003",
                usageType = UsageType.BOTH,
                status = CopyStatus.AVAILABLE
            )
        )

        val request = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to copy.id.toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Member must be checked in to borrow a copy"))
    }

    @Test
    fun `Given reading room only copy When POST api loans Then should return 400`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-READING1",
                firstName = "Reading",
                lastName = "Room",
                email = "reading@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val book = bookRepository.save(
            Book(
                isbn = "978-0-13-235088-7",
                title = "Reference Book"
            )
        )

        val copy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-READING-001",
                usageType = UsageType.READING_ROOM_ONLY,
                status = CopyStatus.AVAILABLE
            )
        )

        visitRepository.save(Visit(member = member))

        val request = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to copy.id.toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Copy is for reading room only"))
    }

    @Test
    fun `Given loaned copy When POST api loans Then should return 400`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-LOANED1",
                firstName = "Loaned",
                lastName = "Test",
                email = "loaned@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val book = bookRepository.save(
            Book(
                isbn = "978-0-13-235088-8",
                title = "Popular Book"
            )
        )

        val copy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-LOANED-001",
                usageType = UsageType.BOTH,
                status = CopyStatus.LOANED
            )
        )

        visitRepository.save(Visit(member = member))

        val request = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to copy.id.toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Copy is not available: LOANED"))
    }

    // ==================== POST /api/loans/{id}/return ====================

    @Test
    fun `Given active loan When POST api loans id return Then should return 200`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-RETURN1",
                firstName = "Return",
                lastName = "Test",
                email = "return@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val book = bookRepository.save(
            Book(
                isbn = "978-0-13-235088-9",
                title = "Returned Book"
            )
        )

        val copy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-RETURN-001",
                usageType = UsageType.BOTH,
                status = CopyStatus.AVAILABLE
            )
        )

        visitRepository.save(Visit(member = member))

        // Create loan first
        val borrowRequest = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to copy.id.toString()
        )

        val borrowResult = mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest))
        ).andReturn()

        val loanId = objectMapper.readTree(borrowResult.response.contentAsString)
            .get("id").asText()

        // When & Then
        mockMvc.perform(post("/api/loans/$loanId/return"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(loanId))
            .andExpect(jsonPath("$.status").value("RETURNED"))
            .andExpect(jsonPath("$.returnedAt").exists())

        // Verify DB
        val returnedCopy = bookCopyRepository.findById(copy.id).orElse(null)
        assertNotNull(returnedCopy)
        assertEquals(CopyStatus.AVAILABLE, returnedCopy.status)
    }

    @Test
    fun `Given non-existing loan When POST api loans id return Then should return 404`() {
        // When & Then
        mockMvc.perform(post("/api/loans/${UUID.randomUUID()}/return"))
            .andExpect(status().isNotFound)
    }

    // ==================== GET /api/loans/{id} ====================

    @Test
    fun `Given existing loan When GET api loans id Then should return 200`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-GET1",
                firstName = "Get",
                lastName = "Test",
                email = "get@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val book = bookRepository.save(
            Book(
                isbn = "978-0-13-235099-0",
                title = "Get Book"
            )
        )

        val copy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-GET-001",
                usageType = UsageType.BOTH,
                status = CopyStatus.AVAILABLE
            )
        )

        visitRepository.save(Visit(member = member))

        val borrowRequest = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to copy.id.toString()
        )

        val borrowResult = mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest))
        ).andReturn()

        val loanId = objectMapper.readTree(borrowResult.response.contentAsString)
            .get("id").asText()

        // When & Then
        mockMvc.perform(get("/api/loans/$loanId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(loanId))
            .andExpect(jsonPath("$.memberName").value("Get Test"))
            .andExpect(jsonPath("$.bookTitle").value("Get Book"))
    }

    @Test
    fun `Given non-existing loan When GET api loans id Then should return 404`() {
        // When & Then
        mockMvc.perform(get("/api/loans/${UUID.randomUUID()}"))
            .andExpect(status().isNotFound)
    }

    // ==================== GET /api/members/{memberId}/loans/active ====================

    @Test
    fun `Given member with active loans When GET api members memberId loans active Then should return list`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-ACTIVE1",
                firstName = "Active",
                lastName = "Loans",
                email = "active@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val book = bookRepository.save(
            Book(
                isbn = "978-0-13-235100-0",
                title = "Active Book"
            )
        )

        val copy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-ACTIVE-001",
                usageType = UsageType.BOTH,
                status = CopyStatus.AVAILABLE
            )
        )

        visitRepository.save(Visit(member = member))

        val borrowRequest = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to copy.id.toString()
        )

        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest))
        )

        // When & Then
        mockMvc.perform(get("/api/members/${member.id}/loans/active"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.loans.length()").value(1))
            .andExpect(jsonPath("$.loans[0].bookTitle").value("Active Book"))
    }

    // ==================== GET /api/members/{memberId}/loans ====================

    @Test
    fun `Given member with loan history When GET api members memberId loans Then should return all loans`() {
        // Given
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-HISTORY1",
                firstName = "History",
                lastName = "Test",
                email = "history@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val book = bookRepository.save(
            Book(
                isbn = "978-0-13-235101-0",
                title = "History Book"
            )
        )

        val copy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-HISTORY-001",
                usageType = UsageType.BOTH,
                status = CopyStatus.AVAILABLE
            )
        )

        visitRepository.save(Visit(member = member))

        val borrowRequest = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to copy.id.toString()
        )

        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest))
        )

        // When & Then
        mockMvc.perform(get("/api/members/${member.id}/loans"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.loans.length()").value(1))
    }

    @Test
    fun `Given non-existing member When GET api members memberId loans Then should return 404`() {
        // When & Then
        mockMvc.perform(get("/api/members/${UUID.randomUUID()}/loans"))
            .andExpect(status().isNotFound)
    }
}
