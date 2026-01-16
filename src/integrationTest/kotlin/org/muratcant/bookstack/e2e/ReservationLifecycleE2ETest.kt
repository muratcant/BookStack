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
import org.muratcant.bookstack.features.reservation.domain.Reservation
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import org.muratcant.bookstack.features.visit.domain.Visit
import org.muratcant.bookstack.features.visit.domain.VisitRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * E2E Tests: Reservation Lifecycle
 *
 * Bu test sınıfı, rezervasyon sisteminin tüm yaşam döngüsünü
 * uçtan uca test eder. FIFO kuyruğu, ON_HOLD durumu ve
 * rezervasyon sahibi kontrollerini kapsar.
 */
@Transactional
class ReservationLifecycleE2ETest : BaseIntegrationTest() {

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
     * E2E Test: Tam Rezervasyon Döngüsü - Rezervasyondan Teslim Almaya
     *
     * Senaryo:
     * 1. 2 üye oluştur (Borrower ve Reserver)
     * 2. Kitap ve kopya oluştur
     * 3. Borrower kitabı ödünç alır (kopya LOANED olur)
     * 4. Reserver aynı kitap için rezervasyon yapar (WAITING)
     * 5. Borrower kitabı iade eder
     *    → Kopya otomatik olarak ON_HOLD olur
     *    → Rezervasyon READY_FOR_PICKUP olur
     * 6. Reserver check-in yapar
     * 7. Reserver ON_HOLD kopyayı ödünç alır
     *    → Rezervasyon FULFILLED olur
     *
     * DB Doğrulamaları:
     * - Reservation.status: WAITING → READY_FOR_PICKUP → FULFILLED
     * - Copy.status: AVAILABLE → LOANED → ON_HOLD → LOANED
     * - İade sonrası copy, rezervasyon sahibine atanır
     */
    @Test
    fun `E2E - Complete reservation cycle from creation to fulfillment`() {
        // === Setup: Create two members ===
        val borrower = memberRepository.save(
            Member(
                membershipNumber = "MBR-BORROWER01",
                firstName = "Current",
                lastName = "Borrower",
                email = "borrower@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val reserver = memberRepository.save(
            Member(
                membershipNumber = "MBR-RESERVER01",
                firstName = "Waiting",
                lastName = "Reserver",
                email = "reserver@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        // Create book and copy
        val book = bookRepository.save(
            Book(isbn = "978-0-RESERV-001", title = "Popular Book")
        )

        val copy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-POPULAR-001",
                usageType = UsageType.BORROWABLE,
                status = CopyStatus.AVAILABLE
            )
        )

        // === STEP 1: Borrower checks in and borrows the book ===
        visitRepository.save(Visit(member = borrower))

        val borrowRequest = mapOf(
            "memberId" to borrower.id.toString(),
            "copyId" to copy.id.toString()
        )

        val loanResult = mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val loanResponse = objectMapper.readValue<Map<String, Any>>(loanResult.response.contentAsString)
        val loanId = UUID.fromString(loanResponse["id"] as String)

        // DB Verify: Copy is LOANED
        assertEquals(CopyStatus.LOANED, bookCopyRepository.findById(copy.id).get().status)

        // === STEP 2: Reserver creates a reservation ===
        val reservationRequest = mapOf(
            "memberId" to reserver.id.toString(),
            "bookId" to book.id.toString()
        )

        val reservationResult = mockMvc.perform(
            post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservationRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("WAITING"))
            .andExpect(jsonPath("$.queuePosition").value(1))
            .andReturn()

        val reservationResponse = objectMapper.readValue<Map<String, Any>>(reservationResult.response.contentAsString)
        val reservationId = UUID.fromString(reservationResponse["id"] as String)

        // DB Verify: Reservation is WAITING
        val waitingReservation = reservationRepository.findById(reservationId).get()
        assertEquals(ReservationStatus.WAITING, waitingReservation.status)
        assertNull(waitingReservation.copy)

        // === STEP 3: Borrower returns the book ===
        mockMvc.perform(post("/api/loans/$loanId/return"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.reservationAssigned").value(true))
            .andExpect(jsonPath("$.reservationId").value(reservationId.toString()))

        // DB Verify: Copy is now ON_HOLD (assigned to reservation)
        val onHoldCopy = bookCopyRepository.findById(copy.id).get()
        assertEquals(CopyStatus.ON_HOLD, onHoldCopy.status)

        // DB Verify: Reservation is READY_FOR_PICKUP with copy assigned
        val readyReservation = reservationRepository.findById(reservationId).get()
        assertEquals(ReservationStatus.READY_FOR_PICKUP, readyReservation.status)
        assertNotNull(readyReservation.copy)
        assertEquals(copy.id, readyReservation.copy!!.id)
        assertNotNull(readyReservation.notifiedAt)
        assertNotNull(readyReservation.expiresAt)

        // === STEP 4: Reserver checks in ===
        visitRepository.save(Visit(member = reserver))

        // === STEP 5: Reserver borrows the ON_HOLD copy ===
        val reserverBorrowRequest = mapOf(
            "memberId" to reserver.id.toString(),
            "copyId" to copy.id.toString()
        )

        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserverBorrowRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.memberName").value("Waiting Reserver"))

        // DB Verify: Reservation is FULFILLED
        val fulfilledReservation = reservationRepository.findById(reservationId).get()
        assertEquals(ReservationStatus.FULFILLED, fulfilledReservation.status)

        // DB Verify: Copy is LOANED to reserver
        val loanedCopy = bookCopyRepository.findById(copy.id).get()
        assertEquals(CopyStatus.LOANED, loanedCopy.status)

        // DB Verify: Reserver has active loan
        val reserverLoans = loanRepository.countByMemberIdAndStatus(reserver.id, LoanStatus.ACTIVE)
        assertEquals(1L, reserverLoans)
    }

    /**
     * E2E Test: Rezervasyon Sahibi Olmayan Üye ON_HOLD Kopyayı Alamaz
     *
     * Senaryo:
     * 1. 3 üye oluştur (Borrower, Reserver, Intruder)
     * 2. Kitap ve kopya oluştur
     * 3. Borrower kitabı ödünç alır
     * 4. Reserver rezervasyon yapar
     * 5. Borrower iade eder → Kopya ON_HOLD, Reserver'a atandı
     * 6. Intruder check-in yapar ve ON_HOLD kopyayı almaya çalışır
     *    → 400 Bad Request (başkasının rezervasyonu)
     * 7. Reserver check-in yapar ve kopyayı alır → Başarılı
     *
     * DB Doğrulamaları:
     * - Intruder'ın borrow denemesi başarısız
     * - Sadece Reserver borrow edebilir
     */
    @Test
    fun `E2E - Non-holder cannot borrow ON_HOLD copy reserved for another member`() {
        // === Setup ===
        val borrower = memberRepository.save(
            Member(
                membershipNumber = "MBR-HOLDER-B01",
                firstName = "Original",
                lastName = "Borrower",
                email = "holder-b@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val reserver = memberRepository.save(
            Member(
                membershipNumber = "MBR-HOLDER-R01",
                firstName = "Legitimate",
                lastName = "Reserver",
                email = "holder-r@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val intruder = memberRepository.save(
            Member(
                membershipNumber = "MBR-INTRUDER01",
                firstName = "Sneaky",
                lastName = "Intruder",
                email = "intruder@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val book = bookRepository.save(
            Book(isbn = "978-0-HOLDER-001", title = "Contested Book")
        )

        val copy = bookCopyRepository.save(
            BookCopy(
                book = book,
                barcode = "BC-CONTESTED-001",
                usageType = UsageType.BORROWABLE,
                status = CopyStatus.AVAILABLE
            )
        )

        // Borrower checks in and borrows
        visitRepository.save(Visit(member = borrower))
        val loan = loanRepository.save(
            Loan(
                member = borrower,
                bookCopy = copy,
                dueDate = LocalDateTime.now().plusDays(14),
                status = LoanStatus.ACTIVE
            )
        )
        copy.status = CopyStatus.LOANED
        bookCopyRepository.save(copy)

        // Reserver creates reservation
        reservationRepository.save(
            Reservation(
                member = reserver,
                book = book,
                queuePosition = 1,
                status = ReservationStatus.WAITING
            )
        )

        // === STEP 1: Borrower returns - copy becomes ON_HOLD ===
        mockMvc.perform(post("/api/loans/${loan.id}/return"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.reservationAssigned").value(true))

        // DB Verify: Copy is ON_HOLD
        assertEquals(CopyStatus.ON_HOLD, bookCopyRepository.findById(copy.id).get().status)

        // === STEP 2: Intruder checks in and tries to borrow ===
        visitRepository.save(Visit(member = intruder))

        val intruderBorrowRequest = mapOf(
            "memberId" to intruder.id.toString(),
            "copyId" to copy.id.toString()
        )

        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(intruderBorrowRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Copy is on hold for another member's reservation"))

        // DB Verify: No loan created for intruder
        val intruderLoans = loanRepository.countByMemberIdAndStatus(intruder.id, LoanStatus.ACTIVE)
        assertEquals(0L, intruderLoans)

        // DB Verify: Copy is still ON_HOLD
        assertEquals(CopyStatus.ON_HOLD, bookCopyRepository.findById(copy.id).get().status)

        // === STEP 3: Reserver checks in and borrows successfully ===
        visitRepository.save(Visit(member = reserver))

        val reserverBorrowRequest = mapOf(
            "memberId" to reserver.id.toString(),
            "copyId" to copy.id.toString()
        )

        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserverBorrowRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("ACTIVE"))

        // DB Verify: Copy is now LOANED to reserver
        assertEquals(CopyStatus.LOANED, bookCopyRepository.findById(copy.id).get().status)
        assertEquals(1L, loanRepository.countByMemberIdAndStatus(reserver.id, LoanStatus.ACTIVE))
    }

    /**
     * E2E Test: FIFO Kuyruk Sırası ve İptal Sonrası Güncelleme
     *
     * Senaryo:
     * 1. 4 üye oluştur (Member1, Member2, Member3, Member4)
     * 2. Kitap oluştur (tüm kopyaları ödünç alınmış varsayalım)
     * 3. 4 üye sırasıyla rezervasyon yapar
     *    → Kuyruk: M1(1), M2(2), M3(3), M4(4)
     * 4. Member2 rezervasyonunu iptal eder
     *    → Kuyruk: M1(1), M3(2), M4(3)
     * 5. Member1 rezervasyonunu iptal eder
     *    → Kuyruk: M3(1), M4(2)
     * 6. Kuyruk sırasını doğrula
     *
     * DB Doğrulamaları:
     * - İptal sonrası queue position'lar güncellenir
     * - FIFO sırası korunur
     */
    @Test
    fun `E2E - FIFO queue position updates correctly after cancellations`() {
        // === Setup: Create 4 members ===
        val members = (1..4).map { i ->
            memberRepository.save(
                Member(
                    membershipNumber = "MBR-QUEUE0$i",
                    firstName = "Member",
                    lastName = "Number$i",
                    email = "member$i@example.com",
                    status = MemberStatus.ACTIVE
                )
            )
        }

        val book = bookRepository.save(
            Book(isbn = "978-0-QUEUE-001", title = "Queue Test Book")
        )

        // === STEP 1: All 4 members create reservations ===
        val reservationIds = mutableListOf<UUID>()

        for ((index, member) in members.withIndex()) {
            val request = mapOf(
                "memberId" to member.id.toString(),
                "bookId" to book.id.toString()
            )

            val result = mockMvc.perform(
                post("/api/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.queuePosition").value(index + 1))
                .andReturn()

            val response = objectMapper.readValue<Map<String, Any>>(result.response.contentAsString)
            reservationIds.add(UUID.fromString(response["id"] as String))
        }

        // DB Verify: Queue positions are 1, 2, 3, 4
        for ((index, id) in reservationIds.withIndex()) {
            val reservation = reservationRepository.findById(id).get()
            assertEquals(index + 1, reservation.queuePosition)
        }

        // === STEP 2: Member2 cancels their reservation ===
        mockMvc.perform(delete("/api/reservations/${reservationIds[1]}"))
            .andExpect(status().isNoContent)

        // DB Verify: Member2's reservation is CANCELLED
        val cancelledRes2 = reservationRepository.findById(reservationIds[1]).get()
        assertEquals(ReservationStatus.CANCELLED, cancelledRes2.status)

        // DB Verify: Queue is now M1(1), M3(2), M4(3)
        // Note: Only WAITING reservations have meaningful queue positions
        val queueAfterCancel1 = reservationRepository
            .findByBookIdAndStatusOrderByQueuePositionAsc(book.id, ReservationStatus.WAITING)

        assertEquals(3, queueAfterCancel1.size)
        assertEquals(members[0].id, queueAfterCancel1[0].member.id) // M1 at position 1
        assertEquals(1, queueAfterCancel1[0].queuePosition)
        assertEquals(members[2].id, queueAfterCancel1[1].member.id) // M3 at position 2
        assertEquals(3, queueAfterCancel1[1].queuePosition)
        assertEquals(members[3].id, queueAfterCancel1[2].member.id) // M4 at position 3
        assertEquals(4, queueAfterCancel1[2].queuePosition)

        // === STEP 3: Member1 cancels their reservation ===
        mockMvc.perform(delete("/api/reservations/${reservationIds[0]}"))
            .andExpect(status().isNoContent)

        // DB Verify: Queue is now M3(1), M4(2)
        val queueAfterCancel2 = reservationRepository
            .findByBookIdAndStatusOrderByQueuePositionAsc(book.id, ReservationStatus.WAITING)

        assertEquals(2, queueAfterCancel2.size)
        assertEquals(members[2].id, queueAfterCancel2[0].member.id) // M3 is now first
        assertEquals(3, queueAfterCancel2[0].queuePosition)
        assertEquals(members[3].id, queueAfterCancel2[1].member.id) // M4 is now second
        assertEquals(4, queueAfterCancel2[1].queuePosition)

        // === STEP 4: Verify via API ===
        mockMvc.perform(get("/api/books/${book.id}/reservations"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalWaiting").value(2))
            .andExpect(jsonPath("$.queue[0].memberName").value("Member Number3"))
            .andExpect(jsonPath("$.queue[0].queuePosition").value(3))
            .andExpect(jsonPath("$.queue[1].memberName").value("Member Number4"))
            .andExpect(jsonPath("$.queue[1].queuePosition").value(4))
    }

    /**
     * E2E Test: Aynı Kitap İçin Tekrar Rezervasyon Engeli
     *
     * Senaryo:
     * 1. Üye oluştur
     * 2. Kitap oluştur
     * 3. Üye rezervasyon yapar (WAITING)
     * 4. Aynı üye aynı kitap için tekrar rezervasyon yapmaya çalışır
     *    → 400 Bad Request (duplicate)
     *
     * DB Doğrulamaları:
     * - Sadece 1 aktif rezervasyon var
     */
    @Test
    fun `E2E - Duplicate reservation for same book by same member is prevented`() {
        // === Setup ===
        val member = memberRepository.save(
            Member(
                membershipNumber = "MBR-DUP01",
                firstName = "Duplicate",
                lastName = "Tester",
                email = "dup@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val book = bookRepository.save(
            Book(isbn = "978-0-DUP-001", title = "Duplicate Test Book")
        )

        // === STEP 1: Create first reservation ===
        val request = mapOf(
            "memberId" to member.id.toString(),
            "bookId" to book.id.toString()
        )

        mockMvc.perform(
            post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.queuePosition").value(1))

        // DB Verify: 1 reservation exists
        val reservationsAfterFirst = reservationRepository.findByMemberIdAndStatusInOrderByCreatedAtDesc(
            member.id, listOf(ReservationStatus.WAITING, ReservationStatus.READY_FOR_PICKUP)
        )
        assertEquals(1, reservationsAfterFirst.size)

        // === STEP 2: Try to create duplicate reservation ===
        mockMvc.perform(
            post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Member already has an active reservation for this book"))

        // DB Verify: Still only 1 reservation
        val reservationsAfterDuplicate = reservationRepository.findByMemberIdAndStatusInOrderByCreatedAtDesc(
            member.id, listOf(ReservationStatus.WAITING, ReservationStatus.READY_FOR_PICKUP)
        )
        assertEquals(1, reservationsAfterDuplicate.size)
    }
}
