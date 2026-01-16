package org.muratcant.bookstack.e2e

import com.fasterxml.jackson.module.kotlin.readValue
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
import org.muratcant.bookstack.features.penalty.domain.PenaltyRepository
import org.muratcant.bookstack.features.penalty.domain.PenaltyStatus
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * E2E Tests: Loan Lifecycle
 *
 * Bu test sınıfı, ödünç alma sürecinin tüm yaşam döngüsünü
 * uçtan uca test eder. Gerçek veritabanı kullanılır (Docker PostgreSQL).
 */
@Transactional
class LoanLifecycleE2ETest : BaseIntegrationTest() {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var bookCopyRepository: BookCopyRepository

    @Autowired
    private lateinit var loanRepository: LoanRepository

    @Autowired
    private lateinit var visitRepository: VisitRepository

    @Autowired
    private lateinit var penaltyRepository: PenaltyRepository

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @BeforeEach
    fun setup() {
        penaltyRepository.deleteAll()
        reservationRepository.deleteAll()
        loanRepository.deleteAll()
        visitRepository.deleteAll()
        bookCopyRepository.deleteAll()
        bookRepository.deleteAll()
        memberRepository.deleteAll()
    }

    /**
     * E2E Test: Tam Ödünç Alma Döngüsü
     *
     * Senaryo:
     * 1. Yeni üye kaydı oluştur (POST /api/members)
     * 2. Kitap oluştur (POST /api/books)
     * 3. Kopya oluştur (POST /api/copies)
     * 4. Üye check-in yapar (POST /api/visits)
     * 5. Üye kitabı ödünç alır (POST /api/loans)
     * 6. Üye check-out yapar (POST /api/visits/{id}/checkout)
     * 7. Üye tekrar check-in yapar
     * 8. Kitabı iade eder (POST /api/loans/{id}/return)
     *
     * DB Doğrulamaları:
     * - Member kaydı oluştu
     * - Book ve Copy kaydı oluştu
     * - Visit kaydı oluştu (check-in/check-out)
     * - Loan.status: ACTIVE → RETURNED
     * - Copy.status: AVAILABLE → LOANED → AVAILABLE
     */
    @Test
    fun `E2E - Complete loan lifecycle from registration to return`() {
        // === STEP 1: Register new member ===
        val memberRequest = mapOf(
            "firstName" to "John",
            "lastName" to "Doe",
            "email" to "john.doe@example.com",
            "phone" to "+90 555 123 4567"
        )

        val memberResult = mockMvc.perform(
            post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andReturn()

        val memberResponse = objectMapper.readValue<Map<String, Any>>(memberResult.response.contentAsString)
        val memberId = UUID.fromString(memberResponse["id"] as String)

        // DB Verify: Member exists
        val member = memberRepository.findById(memberId).orElse(null)
        assertNotNull(member)
        assertEquals("John", member.firstName)
        assertEquals(MemberStatus.ACTIVE, member.status)

        // === STEP 2: Create book ===
        val bookRequest = mapOf(
            "isbn" to "978-3-16-148410-0",
            "title" to "Clean Code",
            "authors" to listOf("Robert C. Martin"),
            "categories" to listOf("Software Engineering")
        )

        val bookResult = mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("Clean Code"))
            .andReturn()

        val bookResponse = objectMapper.readValue<Map<String, Any>>(bookResult.response.contentAsString)
        val bookId = UUID.fromString(bookResponse["id"] as String)

        // DB Verify: Book exists
        val book = bookRepository.findById(bookId).orElse(null)
        assertNotNull(book)
        assertEquals("Clean Code", book.title)

        // === STEP 3: Create book copy ===
        val copyRequest = mapOf(
            "bookId" to bookId.toString(),
            "barcode" to "BC-CLEANCODE-001",
            "usageType" to "BORROWABLE"
        )

        val copyResult = mockMvc.perform(
            post("/api/copies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(copyRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("AVAILABLE"))
            .andReturn()

        val copyResponse = objectMapper.readValue<Map<String, Any>>(copyResult.response.contentAsString)
        val copyId = UUID.fromString(copyResponse["id"] as String)

        // DB Verify: Copy exists and AVAILABLE
        val copy = bookCopyRepository.findById(copyId).orElse(null)
        assertNotNull(copy)
        assertEquals(CopyStatus.AVAILABLE, copy.status)

        // === STEP 4: Member check-in ===
        val visitRequest = mapOf("memberId" to memberId.toString())

        val visitResult = mockMvc.perform(
            post("/api/visits/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(visitRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andReturn()

        val visitResponse = objectMapper.readValue<Map<String, Any>>(visitResult.response.contentAsString)
        val visitId = UUID.fromString(visitResponse["id"] as String)

        // DB Verify: Visit exists, member is checked in
        assertTrue(visitRepository.existsByMemberIdAndCheckOutTimeIsNull(memberId))

        // === STEP 5: Borrow the book ===
        val loanRequest = mapOf(
            "memberId" to memberId.toString(),
            "copyId" to copyId.toString()
        )

        val loanResult = mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.memberName").value("John Doe"))
            .andExpect(jsonPath("$.bookTitle").value("Clean Code"))
            .andReturn()

        val loanResponse = objectMapper.readValue<Map<String, Any>>(loanResult.response.contentAsString)
        val loanId = UUID.fromString(loanResponse["id"] as String)

        // DB Verify: Loan created, Copy status changed to LOANED
        val activeLoan = loanRepository.findById(loanId).orElse(null)
        assertNotNull(activeLoan)
        assertEquals(LoanStatus.ACTIVE, activeLoan.status)

        val loanedCopy = bookCopyRepository.findById(copyId).orElse(null)
        assertEquals(CopyStatus.LOANED, loanedCopy.status)

        // === STEP 6: Member check-out ===
        mockMvc.perform(post("/api/visits/$visitId/checkout"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.checkOutTime").exists())

        // DB Verify: Member is no longer checked in
        val checkedOutVisit = visitRepository.findById(visitId).orElse(null)
        assertNotNull(checkedOutVisit.checkOutTime)

        // === STEP 7: Member check-in again (to return book) ===
        val secondVisitResult = mockMvc.perform(
            post("/api/visits/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(visitRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        // DB Verify: New visit exists
        assertTrue(visitRepository.existsByMemberIdAndCheckOutTimeIsNull(memberId))

        // === STEP 8: Return the book ===
        mockMvc.perform(post("/api/loans/$loanId/return"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("RETURNED"))
            .andExpect(jsonPath("$.isOverdue").value(false))
            .andExpect(jsonPath("$.penaltyId").doesNotExist())

        // DB Verify: Loan returned, Copy available again
        val returnedLoan = loanRepository.findById(loanId).orElse(null)
        assertEquals(LoanStatus.RETURNED, returnedLoan.status)
        assertNotNull(returnedLoan.returnedAt)

        val availableCopy = bookCopyRepository.findById(copyId).orElse(null)
        assertEquals(CopyStatus.AVAILABLE, availableCopy.status)

        // Verify no penalty was created (on-time return)
        val penalties = penaltyRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
        assertTrue(penalties.isEmpty())
    }

    /**
     * E2E Test: Gecikmeli İade + Ceza Oluşturma + Ödeme
     *
     * Senaryo:
     * 1. Üye oluştur ve check-in yap
     * 2. Kitap ve kopya oluştur
     * 3. Geçmiş tarihli dueDate ile loan oluştur (DB'de direkt)
     * 4. Kitabı iade et → Ceza otomatik oluşur
     * 5. Cezayı öde
     *
     * DB Doğrulamaları:
     * - Loan.status: ACTIVE → RETURNED
     * - Penalty oluştu (amount = daysOverdue × 1.00)
     * - Penalty.status: UNPAID → PAID
     */
    @Test
    fun `E2E - Overdue return creates penalty and payment clears it`() {
        // === Setup: Create member, book, copy directly in DB ===
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-OVERDUE01",
                firstName = "Late",
                lastName = "Returner",
                email = "late@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val book = bookRepository.save(
            Book(isbn = "978-0-OVERDUE-001", title = "Overdue Book")
        )

        val copy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-OVERDUE-001",
                usageType = UsageType.BORROWABLE,
                status = CopyStatus.LOANED
            )
        )

        // Create overdue loan (due 5 days ago)
        val loan = loanRepository.save(
            Loan(
                member = member,
                bookCopy = copy,
                borrowedAt = LocalDateTime.now().minusDays(19),
                dueDate = LocalDateTime.now().minusDays(5),
                status = LoanStatus.ACTIVE
            )
        )

        // Verify initial state
        assertEquals(LoanStatus.ACTIVE, loan.status)
        assertEquals(0, penaltyRepository.findByMemberIdOrderByCreatedAtDesc(member.id).size)

        // === STEP 1: Return the overdue book ===
        mockMvc.perform(post("/api/loans/${loan.id}/return"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("RETURNED"))
            .andExpect(jsonPath("$.isOverdue").value(true))
            .andExpect(jsonPath("$.daysOverdue").value(5))
            .andExpect(jsonPath("$.penaltyId").exists())
            .andExpect(jsonPath("$.penaltyAmount").value(5.0))

        // DB Verify: Loan returned
        val returnedLoan = loanRepository.findById(loan.id).orElse(null)
        assertEquals(LoanStatus.RETURNED, returnedLoan.status)

        // DB Verify: Penalty created
        val penalties = penaltyRepository.findByMemberIdOrderByCreatedAtDesc(member.id)
        assertEquals(1, penalties.size)
        val penalty = penalties[0]
        assertEquals(PenaltyStatus.UNPAID, penalty.status)
        assertEquals(BigDecimal("5.00"), penalty.amount)
        assertEquals(5, penalty.daysOverdue)
        assertNull(penalty.paidAt)

        // DB Verify: Copy is available again
        val returnedCopy = bookCopyRepository.findById(copy.id).orElse(null)
        assertEquals(CopyStatus.AVAILABLE, returnedCopy.status)

        // === STEP 2: Pay the penalty ===
        mockMvc.perform(post("/api/penalties/${penalty.id}/pay"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("PAID"))
            .andExpect(jsonPath("$.paidAt").exists())

        // DB Verify: Penalty paid
        val paidPenalty = penaltyRepository.findById(penalty.id).orElse(null)
        assertEquals(PenaltyStatus.PAID, paidPenalty.status)
        assertNotNull(paidPenalty.paidAt)

        // DB Verify: No unpaid penalties remain
        val unpaidAmount = penaltyRepository.sumUnpaidAmountByMemberId(member.id)
        assertEquals(BigDecimal.ZERO, unpaidAmount)
    }

    /**
     * E2E Test: Yüksek Ödenmemiş Ceza Ödünç Almayı Engeller
     *
     * Senaryo:
     * 1. Üye oluştur
     * 2. Üyeye yüksek tutarlı ödenmemiş ceza ekle (threshold üzeri: 15 TL > 10 TL)
     * 3. Üye check-in yapar
     * 4. Yeni kitap ödünç almaya çalışır
     * 5. → 400 Bad Request (ceza engeli)
     * 6. Cezayı öde
     * 7. Tekrar ödünç almayı dene → Başarılı
     *
     * DB Doğrulamaları:
     * - İlk borrow denemesi başarısız
     * - Ceza ödendikten sonra borrow başarılı
     * - Loan kaydı sadece ikinci denemede oluşur
     */
    @Test
    fun `E2E - Penalty above threshold blocks borrowing until paid`() {
        // === Setup ===
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-BLOCKED01",
                firstName = "Blocked",
                lastName = "Member",
                email = "blocked@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val oldBook = bookRepository.save(
            Book(isbn = "978-0-OLD-001", title = "Old Book")
        )

        val oldCopy = bookCopyRepository.save(
            BookCopy(
                book = oldBook,
                barcode = "BC-OLD-001",
                usageType = UsageType.BORROWABLE,
                status = CopyStatus.AVAILABLE
            )
        )

        // Create old returned loan for penalty
        val oldLoan = loanRepository.save(
            Loan(
                member = member,
                bookCopy = oldCopy,
                borrowedAt = LocalDateTime.now().minusDays(30),
                dueDate = LocalDateTime.now().minusDays(16),
                returnedAt = LocalDateTime.now().minusDays(1),
                status = LoanStatus.RETURNED
            )
        )

        // Create high penalty (15 TL > 10 TL threshold)
        val penalty = penaltyRepository.save(
            org.muratcant.bookstack.features.penalty.domain.Penalty(
                member = member,
                loan = oldLoan,
                amount = BigDecimal("15.00"),
                daysOverdue = 15,
                status = PenaltyStatus.UNPAID
            )
        )

        // Create new book to borrow
        val newBook = bookRepository.save(
            Book(isbn = "978-0-NEW-001", title = "New Book")
        )

        val newCopy = bookCopyRepository.save(
            BookCopy(
                book = newBook,
                barcode = "BC-NEW-001",
                usageType = UsageType.BORROWABLE,
                status = CopyStatus.AVAILABLE
            )
        )

        // Check-in member
        visitRepository.save(
            org.muratcant.bookstack.features.visit.domain.Visit(member = member)
        )

        // Verify member is checked in
        assertTrue(visitRepository.existsByMemberIdAndCheckOutTimeIsNull(member.id))

        // === STEP 1: Try to borrow - should fail due to penalty ===
        val borrowRequest = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to newCopy.id.toString()
        )

        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Member has unpaid penalties (15.00) above blocking threshold (10.00)"))

        // DB Verify: No loan was created
        val loansAfterFailedAttempt = loanRepository.countByMemberIdAndStatus(member.id, LoanStatus.ACTIVE)
        assertEquals(0L, loansAfterFailedAttempt)

        // === STEP 2: Pay the penalty ===
        mockMvc.perform(post("/api/penalties/${penalty.id}/pay"))
            .andExpect(status().isOk)

        // DB Verify: Penalty paid, unpaid amount is now 0
        val unpaidAfterPayment = penaltyRepository.sumUnpaidAmountByMemberId(member.id)
        assertEquals(BigDecimal.ZERO, unpaidAfterPayment)

        // === STEP 3: Try to borrow again - should succeed ===
        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("ACTIVE"))

        // DB Verify: Loan was created
        val loansAfterSuccess = loanRepository.countByMemberIdAndStatus(member.id, LoanStatus.ACTIVE)
        assertEquals(1L, loansAfterSuccess)

        // DB Verify: Copy status changed to LOANED
        val borrowedCopy = bookCopyRepository.findById(newCopy.id).orElse(null)
        assertEquals(CopyStatus.LOANED, borrowedCopy.status)
    }

    /**
     * E2E Test: Maksimum Aktif Ödünç Limiti
     *
     * Senaryo:
     * 1. Üye oluştur (maxActiveLoans = 5)
     * 2. Üye check-in yapar
     * 3. 5 kitap ödünç alır (limit dolar)
     * 4. 6. kitabı almaya çalışır → 400 Bad Request
     * 5. 1 kitap iade eder
     * 6. 6. kitabı tekrar almaya çalışır → Başarılı
     *
     * DB Doğrulamaları:
     * - Her ödünç almada loan sayısı artar
     * - 5. ödünçten sonra 6. başarısız
     * - İade sonrası loan sayısı düşer
     * - 6. ödünç başarılı olur
     */
    @Test
    fun `E2E - Max loan limit enforcement and recovery after return`() {
        // === Setup: Create member ===
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-MAXLOAN01",
                firstName = "Max",
                lastName = "Borrower",
                email = "maxloan@example.com",
                status = MemberStatus.ACTIVE,
                maxActiveLoans = 5
            )
        )

        // Check-in member
        visitRepository.save(
            org.muratcant.bookstack.features.visit.domain.Visit(member = member)
        )

        // Create 6 books and copies
        val copies = mutableListOf<BookCopy>()
        for (i in 1..6) {
            val book = bookRepository.save(
                Book(isbn = "978-0-MAX-00$i", title = "Max Loan Book $i")
            )
            val copy = bookCopyRepository.save(
                BookCopy(
                    book = book,
                    barcode = "BC-MAX-00$i",
                    usageType = UsageType.BORROWABLE,
                    status = CopyStatus.AVAILABLE
                )
            )
            copies.add(copy)
        }

        // === STEP 1: Borrow 5 books successfully ===
        val loanIds = mutableListOf<UUID>()
        for (i in 0..4) {
            val borrowRequest = mapOf(
                "memberId" to member.id.toString(),
                "copyId" to copies[i].id.toString()
            )

            val result = mockMvc.perform(
                post("/api/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(borrowRequest))
            )
                .andExpect(status().isCreated)
                .andReturn()

            val response = objectMapper.readValue<Map<String, Any>>(result.response.contentAsString)
            loanIds.add(UUID.fromString(response["id"] as String))

            // DB Verify: Loan count increases
            val activeLoans = loanRepository.countByMemberIdAndStatus(member.id, LoanStatus.ACTIVE)
            assertEquals((i + 1).toLong(), activeLoans)
        }

        // DB Verify: 5 active loans
        assertEquals(5L, loanRepository.countByMemberIdAndStatus(member.id, LoanStatus.ACTIVE))

        // === STEP 2: Try to borrow 6th book - should fail ===
        val sixthBorrowRequest = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to copies[5].id.toString()
        )

        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sixthBorrowRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Member has reached maximum active loans limit: 5"))

        // DB Verify: Still 5 active loans, 6th copy still available
        assertEquals(5L, loanRepository.countByMemberIdAndStatus(member.id, LoanStatus.ACTIVE))
        val sixthCopy = bookCopyRepository.findById(copies[5].id).orElse(null)
        assertEquals(CopyStatus.AVAILABLE, sixthCopy.status)

        // === STEP 3: Return first book ===
        mockMvc.perform(post("/api/loans/${loanIds[0]}/return"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("RETURNED"))

        // DB Verify: 4 active loans now
        assertEquals(4L, loanRepository.countByMemberIdAndStatus(member.id, LoanStatus.ACTIVE))

        // === STEP 4: Try to borrow 6th book again - should succeed ===
        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sixthBorrowRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("ACTIVE"))

        // DB Verify: 5 active loans again
        assertEquals(5L, loanRepository.countByMemberIdAndStatus(member.id, LoanStatus.ACTIVE))

        // DB Verify: 6th copy is now LOANED
        val borrowedSixthCopy = bookCopyRepository.findById(copies[5].id).orElse(null)
        assertEquals(CopyStatus.LOANED, borrowedSixthCopy.status)
    }
}
