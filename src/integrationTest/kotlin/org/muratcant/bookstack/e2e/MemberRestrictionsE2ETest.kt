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
import org.muratcant.bookstack.features.loan.domain.LoanRepository
import org.muratcant.bookstack.features.loan.domain.LoanStatus
import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.domain.MemberStatus
import org.muratcant.bookstack.features.penalty.domain.PenaltyRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import org.muratcant.bookstack.features.visit.domain.Visit
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * E2E Tests: Member Restrictions
 *
 * Bu test sınıfı, üye durumu ve kopya türüne göre uygulanan
 * kısıtlamaları uçtan uca test eder. Suspended üye, reading room
 * only kopya ve concurrent visit senaryolarını kapsar.
 */
@Transactional
class MemberRestrictionsE2ETest : BaseIntegrationTest() {

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
     * E2E Test: Suspended Üye Tüm İşlemlerden Engellenir
     *
     * Senaryo:
     * 1. Aktif üye oluştur
     * 2. Üyeyi suspend et
     * 3. Check-in denemesi → 400 Bad Request
     * 4. Ödünç alma denemesi (DB'de check-in olmadan) → 400 Bad Request
     * 5. Rezervasyon denemesi → 400 Bad Request
     * 6. Üyeyi tekrar aktif et
     * 7. Check-in yapabilir → 201 Created
     *
     * DB Doğrulamaları:
     * - Suspended iken hiçbir işlem yapılamaz
     * - Aktif edildikten sonra işlemler başarılı
     */
    @Test
    fun `E2E - Suspended member is blocked from all operations until reactivated`() {
        // === Setup ===
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-SUSPEND01",
                firstName = "To Be",
                lastName = "Suspended",
                email = "suspend@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val book = bookRepository.save(
            Book(isbn = "978-0-SUSPEND-001", title = "Suspended Test Book")
        )

        val copy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-SUSPEND-001",
                usageType = UsageType.BORROWABLE,
                status = CopyStatus.AVAILABLE
            )
        )

        // === STEP 1: Suspend the member via API ===
        mockMvc.perform(patch("/api/members/${member.id}/suspend"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("SUSPENDED"))

        // DB Verify: Member is SUSPENDED
        val suspendedMember = memberRepository.findById(member.id).get()
        assertEquals(MemberStatus.SUSPENDED, suspendedMember.status)

        // === STEP 2: Try to check-in - should fail ===
        val visitRequest = mapOf("memberId" to member.id.toString())

        mockMvc.perform(
            post("/api/visits/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(visitRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Member is not active: SUSPENDED"))

        // DB Verify: No visit created
        assertFalse(visitRepository.existsByMemberIdAndCheckOutTimeIsNull(member.id))

        // === STEP 3: Try to create reservation - should fail ===
        val reservationRequest = mapOf(
            "memberId" to member.id.toString(),
            "bookId" to book.id.toString()
        )

        mockMvc.perform(
            post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservationRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Member is not active: SUSPENDED"))

        // DB Verify: No reservation created
        val reservations = reservationRepository.findByMemberIdAndStatusInOrderByCreatedAtDesc(
            member.id, listOf(ReservationStatus.WAITING, ReservationStatus.READY_FOR_PICKUP)
        )
        assertTrue(reservations.isEmpty())

        // === STEP 4: Force check-in in DB and try to borrow - should still fail ===
        visitRepository.save(Visit(member = suspendedMember))

        val borrowRequest = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to copy.id.toString()
        )

        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Member is not active: SUSPENDED"))

        // DB Verify: No loan created
        assertEquals(0L, loanRepository.countByMemberIdAndStatus(member.id, LoanStatus.ACTIVE))

        // Clean up visit for next step
        visitRepository.deleteAll()

        // === STEP 5: Activate the member ===
        mockMvc.perform(patch("/api/members/${member.id}/activate"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("ACTIVE"))

        // DB Verify: Member is ACTIVE again
        val activatedMember = memberRepository.findById(member.id).get()
        assertEquals(MemberStatus.ACTIVE, activatedMember.status)

        // === STEP 6: Check-in should now succeed ===
        mockMvc.perform(
            post("/api/visits/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(visitRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())

        // DB Verify: Visit created
        assertTrue(visitRepository.existsByMemberIdAndCheckOutTimeIsNull(member.id))

        // === STEP 7: Borrow should now succeed ===
        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("ACTIVE"))

        // DB Verify: Loan created
        assertEquals(1L, loanRepository.countByMemberIdAndStatus(member.id, LoanStatus.ACTIVE))
    }

    /**
     * E2E Test: Reading Room Only Kopya Ödünç Alınamaz
     *
     * Senaryo:
     * 1. Üye oluştur ve check-in yap
     * 2. Kitap ve READING_ROOM_ONLY kopya oluştur
     * 3. Ödünç almaya çalış → 400 Bad Request
     * 4. BORROWABLE kopya oluştur
     * 5. BORROWABLE kopyayı ödünç al → Başarılı
     *
     * DB Doğrulamaları:
     * - READING_ROOM_ONLY kopya ödünç alınamaz
     * - BORROWABLE kopya ödünç alınabilir
     */
    @Test
    fun `E2E - Reading room only copy cannot be borrowed but borrowable can`() {
        // === Setup ===
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-READROOM01",
                firstName = "Reading",
                lastName = "Room",
                email = "readroom@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val book = bookRepository.save(
            Book(isbn = "978-0-READROOM-001", title = "Reference Book")
        )

        val readingRoomCopy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-READROOM-001",
                usageType = UsageType.READING_ROOM_ONLY,
                status = CopyStatus.AVAILABLE
            )
        )

        val borrowableCopy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-BORROWABLE-001",
                usageType = UsageType.BORROWABLE,
                status = CopyStatus.AVAILABLE
            )
        )

        // Check-in member
        visitRepository.save(Visit(member = member))

        // === STEP 1: Try to borrow READING_ROOM_ONLY copy - should fail ===
        val readingRoomBorrowRequest = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to readingRoomCopy.id.toString()
        )

        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readingRoomBorrowRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Copy is for reading room only"))

        // DB Verify: No loan created, copy still AVAILABLE
        assertEquals(0L, loanRepository.countByMemberIdAndStatus(member.id, LoanStatus.ACTIVE))
        assertEquals(CopyStatus.AVAILABLE, bookCopyRepository.findById(readingRoomCopy.id).get().status)

        // === STEP 2: Borrow BORROWABLE copy - should succeed ===
        val borrowableRequest = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to borrowableCopy.id.toString()
        )

        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowableRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.barcode").value("BC-BORROWABLE-001"))

        // DB Verify: Loan created for BORROWABLE copy
        assertEquals(1L, loanRepository.countByMemberIdAndStatus(member.id, LoanStatus.ACTIVE))
        assertEquals(CopyStatus.LOANED, bookCopyRepository.findById(borrowableCopy.id).get().status)

        // DB Verify: READING_ROOM_ONLY copy is still AVAILABLE
        assertEquals(CopyStatus.AVAILABLE, bookCopyRepository.findById(readingRoomCopy.id).get().status)
    }

    /**
     * E2E Test: Aynı Anda İki Kez Check-In Engeli
     *
     * Senaryo:
     * 1. Üye oluştur
     * 2. Check-in yap (aktif ziyaret başlar)
     * 3. Tekrar check-in yapmaya çalış → 400 Bad Request
     * 4. Check-out yap
     * 5. Tekrar check-in yap → Başarılı
     *
     * DB Doğrulamaları:
     * - Aynı anda sadece 1 aktif ziyaret olabilir
     * - Check-out sonrası yeni ziyaret başlatılabilir
     */
    @Test
    fun `E2E - Concurrent check-in is prevented for same member`() {
        // === Setup ===
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-CONCURRENT01",
                firstName = "Concurrent",
                lastName = "Visitor",
                email = "concurrent@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val visitRequest = mapOf("memberId" to member.id.toString())

        // === STEP 1: First check-in ===
        val firstVisitResult = mockMvc.perform(
            post("/api/visits/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(visitRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andReturn()

        val firstVisitResponse = objectMapper.readValue<Map<String, Any>>(firstVisitResult.response.contentAsString)
        val firstVisitId = firstVisitResponse["id"] as String

        // DB Verify: Active visit exists
        assertTrue(visitRepository.existsByMemberIdAndCheckOutTimeIsNull(member.id))
        assertEquals(1, visitRepository.findAll().count { it.member.id == member.id && it.checkOutTime == null })

        // === STEP 2: Try second check-in while still inside - should fail ===
        mockMvc.perform(
            post("/api/visits/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(visitRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Member is already checked in"))

        // DB Verify: Still only 1 active visit
        assertEquals(1, visitRepository.findAll().count { it.member.id == member.id && it.checkOutTime == null })

        // === STEP 3: Check-out ===
        mockMvc.perform(post("/api/visits/$firstVisitId/checkout"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.checkOutTime").exists())

        // DB Verify: No active visit
        assertFalse(visitRepository.existsByMemberIdAndCheckOutTimeIsNull(member.id))

        // === STEP 4: Check-in again - should succeed ===
        mockMvc.perform(
            post("/api/visits/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(visitRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())

        // DB Verify: New active visit exists, total 2 visits (1 completed, 1 active)
        assertTrue(visitRepository.existsByMemberIdAndCheckOutTimeIsNull(member.id))
        assertEquals(2, visitRepository.findAll().count { it.member.id == member.id })
    }

    /**
     * E2E Test: Check-In Olmadan Ödünç Alma Engeli
     *
     * Senaryo:
     * 1. Üye oluştur (check-in yapmadan)
     * 2. Kitap ve kopya oluştur
     * 3. Ödünç almaya çalış → 400 Bad Request (check-in gerekli)
     * 4. Check-in yap
     * 5. Ödünç al → Başarılı
     *
     * DB Doğrulamaları:
     * - Check-in olmadan ödünç alınamaz
     * - Check-in sonrası ödünç alınabilir
     */
    @Test
    fun `E2E - Borrowing requires active check-in`() {
        // === Setup ===
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-NOCHECKIN01",
                firstName = "No",
                lastName = "CheckIn",
                email = "nocheckin@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val book = bookRepository.save(
            Book(isbn = "978-0-NOCHECKIN-001", title = "Check-In Required Book")
        )

        val copy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-NOCHECKIN-001",
                usageType = UsageType.BORROWABLE,
                status = CopyStatus.AVAILABLE
            )
        )

        val borrowRequest = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to copy.id.toString()
        )

        // === STEP 1: Try to borrow without check-in - should fail ===
        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Member must be checked in to borrow a copy"))

        // DB Verify: No loan created
        assertEquals(0L, loanRepository.countByMemberIdAndStatus(member.id, LoanStatus.ACTIVE))

        // === STEP 2: Check-in ===
        val visitRequest = mapOf("memberId" to member.id.toString())
        mockMvc.perform(
            post("/api/visits/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(visitRequest))
        )
            .andExpect(status().isCreated)

        // === STEP 3: Borrow after check-in - should succeed ===
        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("ACTIVE"))

        // DB Verify: Loan created
        assertEquals(1L, loanRepository.countByMemberIdAndStatus(member.id, LoanStatus.ACTIVE))
    }

    /**
     * E2E Test: BOTH UsageType Kopya Hem Ödünç Alınabilir Hem Okuma Salonunda Kullanılabilir
     *
     * Senaryo:
     * 1. Üye oluştur ve check-in yap
     * 2. BOTH tipinde kopya oluştur
     * 3. Ödünç al → Başarılı (BORROWABLE gibi davranır)
     *
     * DB Doğrulamaları:
     * - BOTH tipindeki kopya ödünç alınabilir
     */
    @Test
    fun `E2E - BOTH usage type copy can be borrowed`() {
        // === Setup ===
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-BOTH01",
                firstName = "Both",
                lastName = "User",
                email = "both@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val book = bookRepository.save(
            Book(isbn = "978-0-BOTH-001", title = "Versatile Book")
        )

        val bothCopy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-BOTH-001",
                usageType = UsageType.BOTH,
                status = CopyStatus.AVAILABLE
            )
        )

        // Check-in member
        visitRepository.save(Visit(member = member))

        // === STEP 1: Borrow BOTH type copy - should succeed ===
        val borrowRequest = mapOf(
            "memberId" to member.id.toString(),
            "copyId" to bothCopy.id.toString()
        )

        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("ACTIVE"))

        // DB Verify: Loan created
        assertEquals(1L, loanRepository.countByMemberIdAndStatus(member.id, LoanStatus.ACTIVE))
        assertEquals(CopyStatus.LOANED, bookCopyRepository.findById(bothCopy.id).get().status)
    }
}
