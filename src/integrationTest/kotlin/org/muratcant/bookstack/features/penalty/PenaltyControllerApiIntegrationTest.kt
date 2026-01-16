package org.muratcant.bookstack.features.penalty

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.muratcant.bookstack.BaseIntegrationTest
import org.muratcant.bookstack.features.book.domain.Book
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.bookcopy.domain.BookCopy
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.bookcopy.domain.UsageType
import org.muratcant.bookstack.features.loan.domain.Loan
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.muratcant.bookstack.features.loan.domain.LoanStatus
import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.domain.MemberStatus
import org.muratcant.bookstack.features.penalty.domain.Penalty
import org.muratcant.bookstack.features.penalty.domain.PenaltyRepository
import org.muratcant.bookstack.features.penalty.domain.PenaltyStatus
import org.muratcant.bookstack.features.visit.domain.Visit
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Transactional
class PenaltyControllerApiIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var penaltyRepository: PenaltyRepository

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

    private lateinit var testMember: Member
    private lateinit var testBook: Book
    private lateinit var testCopy: BookCopy
    private lateinit var testLoan: Loan

    @BeforeEach
    fun setup() {
        penaltyRepository.deleteAll()
        loanRepository.deleteAll()
        visitRepository.deleteAll()
        bookCopyRepository.deleteAll()
        bookRepository.deleteAll()
        memberRepository.deleteAll()

        testMember = memberRepository.save(
            Member(
                membershipNumber = "MBR-PENALTY01",
                firstName = "John",
                lastName = "Doe",
                email = "penalty@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        testBook = bookRepository.save(
            Book(
                isbn = "978-0-PENALTY-001",
                title = "Penalty Test Book"
            )
        )

        testCopy = bookCopyRepository.save(
            BookCopy(
                book = testBook,
                barcode = "BC-PENALTY-001",
                usageType = UsageType.BOTH,
                status = CopyStatus.AVAILABLE
            )
        )

        testLoan = loanRepository.save(
            Loan(
                member = testMember,
                bookCopy = testCopy,
                borrowedAt = LocalDateTime.now().minusDays(20),
                dueDate = LocalDateTime.now().minusDays(6),
                returnedAt = LocalDateTime.now(),
                status = LoanStatus.RETURNED
            )
        )
    }

    // ==================== GET /api/penalties ====================

    @Test
    fun `Given penalties When GET api penalties Then should return list`() {
        // Given
        penaltyRepository.save(
            Penalty(
                member = testMember,
                loan = testLoan,
                amount = BigDecimal("5.00"),
                daysOverdue = 5
            )
        )

        // When & Then
        mockMvc.perform(get("/api/penalties"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.penalties.length()").value(1))
            .andExpect(jsonPath("$.penalties[0].memberName").value("John Doe"))
            .andExpect(jsonPath("$.penalties[0].amount").value(5.0))
    }

    @Test
    fun `Given no penalties When GET api penalties Then should return empty list`() {
        // When & Then
        mockMvc.perform(get("/api/penalties"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.penalties.length()").value(0))
    }

    // ==================== GET /api/penalties/{id} ====================

    @Test
    fun `Given existing penalty When GET api penalties id Then should return 200`() {
        // Given
        val penalty = penaltyRepository.save(
            Penalty(
                member = testMember,
                loan = testLoan,
                amount = BigDecimal("5.00"),
                daysOverdue = 5
            )
        )

        // When & Then
        mockMvc.perform(get("/api/penalties/${penalty.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(penalty.id.toString()))
            .andExpect(jsonPath("$.memberId").value(testMember.id.toString()))
            .andExpect(jsonPath("$.memberName").value("John Doe"))
            .andExpect(jsonPath("$.amount").value(5.0))
            .andExpect(jsonPath("$.daysOverdue").value(5))
            .andExpect(jsonPath("$.status").value("UNPAID"))
    }

    @Test
    fun `Given non-existing penalty When GET api penalties id Then should return 404`() {
        // When & Then
        mockMvc.perform(get("/api/penalties/${UUID.randomUUID()}"))
            .andExpect(status().isNotFound)
    }

    // ==================== GET /api/members/{memberId}/penalties ====================

    @Test
    fun `Given member with penalties When GET api members memberId penalties Then should return list`() {
        // Given
        penaltyRepository.save(
            Penalty(
                member = testMember,
                loan = testLoan,
                amount = BigDecimal("5.00"),
                daysOverdue = 5
            )
        )

        // When & Then
        mockMvc.perform(get("/api/members/${testMember.id}/penalties"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalUnpaidAmount").value(5.0))
            .andExpect(jsonPath("$.penalties.length()").value(1))
    }

    @Test
    fun `Given member without penalties When GET api members memberId penalties Then should return empty`() {
        // When & Then
        mockMvc.perform(get("/api/members/${testMember.id}/penalties"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalUnpaidAmount").value(0))
            .andExpect(jsonPath("$.penalties.length()").value(0))
    }

    @Test
    fun `Given non-existing member When GET api members memberId penalties Then should return 404`() {
        // When & Then
        mockMvc.perform(get("/api/members/${UUID.randomUUID()}/penalties"))
            .andExpect(status().isNotFound)
    }

    // ==================== POST /api/penalties/{id}/pay ====================

    @Test
    fun `Given unpaid penalty When POST api penalties id pay Then should return 200`() {
        // Given
        val penalty = penaltyRepository.save(
            Penalty(
                member = testMember,
                loan = testLoan,
                amount = BigDecimal("5.00"),
                daysOverdue = 5,
                status = PenaltyStatus.UNPAID
            )
        )

        // When & Then
        mockMvc.perform(post("/api/penalties/${penalty.id}/pay"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(penalty.id.toString()))
            .andExpect(jsonPath("$.status").value("PAID"))
            .andExpect(jsonPath("$.paidAt").exists())

        // Verify DB
        val paidPenalty = penaltyRepository.findById(penalty.id).orElse(null)
        assertNotNull(paidPenalty)
        assertEquals(PenaltyStatus.PAID, paidPenalty.status)
    }

    @Test
    fun `Given already paid penalty When POST api penalties id pay Then should return 400`() {
        // Given
        val penalty = penaltyRepository.save(
            Penalty(
                member = testMember,
                loan = testLoan,
                amount = BigDecimal("5.00"),
                daysOverdue = 5,
                status = PenaltyStatus.PAID,
                paidAt = LocalDateTime.now()
            )
        )

        // When & Then
        mockMvc.perform(post("/api/penalties/${penalty.id}/pay"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Penalty is already paid"))
    }

    @Test
    fun `Given non-existing penalty When POST api penalties id pay Then should return 404`() {
        // When & Then
        mockMvc.perform(post("/api/penalties/${UUID.randomUUID()}/pay"))
            .andExpect(status().isNotFound)
    }

    // ==================== Integration: Return overdue loan creates penalty ====================

    @Test
    fun `Given overdue loan When return Then should create penalty automatically`() {
        // Given - Create new loan that is overdue
        val newMember = memberRepository.save(
            Member(
                membershipNumber = "MBR-OVERDUE01",
                firstName = "Overdue",
                lastName = "Member",
                email = "overdue@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val newBook = bookRepository.save(
            Book(
                isbn = "978-0-OVERDUE-001",
                title = "Overdue Book"
            )
        )

        val newCopy = bookCopyRepository.save(
            BookCopy(
                book = newBook,
                barcode = "BC-OVERDUE-001",
                usageType = UsageType.BOTH,
                status = CopyStatus.LOANED
            )
        )

        val overdueLoan = loanRepository.save(
            Loan(
                member = newMember,
                bookCopy = newCopy,
                borrowedAt = LocalDateTime.now().minusDays(20),
                dueDate = LocalDateTime.now().minusDays(5),
                status = LoanStatus.ACTIVE
            )
        )

        // When - Return the overdue loan
        mockMvc.perform(post("/api/loans/${overdueLoan.id}/return"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.overdue").value(true))
            .andExpect(jsonPath("$.daysOverdue").value(5))
            .andExpect(jsonPath("$.penaltyId").exists())
            .andExpect(jsonPath("$.penaltyAmount").value(5.0))

        // Then - Verify penalty was created
        val penalties = penaltyRepository.findByMemberIdOrderByCreatedAtDesc(newMember.id)
        assertEquals(1, penalties.size)
        assertEquals(BigDecimal("5.0"), penalties.first().amount)
        assertEquals(5, penalties[0].daysOverdue)
    }

    // ==================== Integration: Unpaid penalties block borrowing ====================

    @Test
    fun `Given member with high unpaid penalties When borrow Then should return 400`() {
        // Given - Create member with high unpaid penalty
        val blockedMember = memberRepository.save(
            Member(
                membershipNumber = "MBR-BLOCKED01",
                firstName = "Blocked",
                lastName = "Member",
                email = "blocked@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val blockedBook = bookRepository.save(
            Book(
                isbn = "978-0-BLOCKED-001",
                title = "Blocked Book"
            )
        )

        val blockedCopy = bookCopyRepository.save(
            BookCopy(
                book = blockedBook,
                barcode = "BC-BLOCKED-001",
                usageType = UsageType.BOTH,
                status = CopyStatus.LOANED
            )
        )

        val oldLoan = loanRepository.save(
            Loan(
                member = blockedMember,
                bookCopy = blockedCopy,
                borrowedAt = LocalDateTime.now().minusDays(30),
                dueDate = LocalDateTime.now().minusDays(16),
                returnedAt = LocalDateTime.now().minusDays(1),
                status = LoanStatus.RETURNED
            )
        )

        // Create high penalty (above threshold of 10.00)
        penaltyRepository.save(
            Penalty(
                member = blockedMember,
                loan = oldLoan,
                amount = BigDecimal("15.00"),
                daysOverdue = 15,
                status = PenaltyStatus.UNPAID
            )
        )

        // Create visit for member
        visitRepository.save(Visit(member = blockedMember))

        // Create new available copy for borrowing attempt
        val newCopy = bookCopyRepository.save(
            BookCopy(
                book = blockedBook,
                barcode = "BC-BLOCKED-002",
                usageType = UsageType.BOTH,
                status = CopyStatus.AVAILABLE
            )
        )

        val borrowRequest = mapOf(
            "memberId" to blockedMember.id.toString(),
            "copyId" to newCopy.id.toString()
        )

        // When & Then - Try to borrow should fail
        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Member has unpaid penalties (15.00) above blocking threshold (10.0)"))
    }
}
