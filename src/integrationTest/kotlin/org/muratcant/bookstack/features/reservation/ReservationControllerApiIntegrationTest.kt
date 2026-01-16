package org.muratcant.bookstack.features.reservation

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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Transactional
class ReservationControllerApiIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

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

    private lateinit var testMember: Member
    private lateinit var testBook: Book
    private lateinit var testCopy: BookCopy

    @BeforeEach
    fun setup() {
        penaltyRepository.deleteAll()
        reservationRepository.deleteAll()
        loanRepository.deleteAll()
        visitRepository.deleteAll()
        bookCopyRepository.deleteAll()
        bookRepository.deleteAll()
        memberRepository.deleteAll()

        testMember = memberRepository.save(
            Member(
                membershipNumber = "MBR-RESERV01",
                firstName = "John",
                lastName = "Doe",
                email = "reserv@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        testBook = bookRepository.save(
            Book(
                isbn = "978-0-RESERV-001",
                title = "Reservation Test Book"
            )
        )

        testCopy = bookCopyRepository.save(
            BookCopy(
                book = testBook,
                barcode = "BC-RESERV-001",
                usageType = UsageType.BOTH,
                status = CopyStatus.LOANED
            )
        )
    }

    // ==================== POST /api/reservations ====================

    @Test
    fun `Given active member and book When POST api reservations Then should return 201`() {
        // Given
        val request = mapOf(
            "memberId" to testMember.id.toString(),
            "bookId" to testBook.id.toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.memberId").value(testMember.id.toString()))
            .andExpect(jsonPath("$.bookId").value(testBook.id.toString()))
            .andExpect(jsonPath("$.queuePosition").value(1))
            .andExpect(jsonPath("$.status").value("WAITING"))
    }

    @Test
    fun `Given multiple reservations When POST api reservations Then should assign correct queue position`() {
        // Given - Create existing reservations
        reservationRepository.save(
            Reservation(member = testMember, book = testBook, queuePosition = 1)
        )

        val newMember = memberRepository.save(
            Member(
                membershipNumber = "MBR-RESERV02",
                firstName = "Jane",
                lastName = "Doe",
                email = "jane@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val request = mapOf(
            "memberId" to newMember.id.toString(),
            "bookId" to testBook.id.toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.queuePosition").value(2))
    }

    @Test
    fun `Given member already has reservation When POST api reservations Then should return 400`() {
        // Given
        reservationRepository.save(
            Reservation(member = testMember, book = testBook, queuePosition = 1)
        )

        val request = mapOf(
            "memberId" to testMember.id.toString(),
            "bookId" to testBook.id.toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Member already has an active reservation for this book"))
    }

    @Test
    fun `Given suspended member When POST api reservations Then should return 400`() {
        // Given
        val suspendedMember = memberRepository.save(
            Member(
                membershipNumber = "MBR-SUSP01",
                firstName = "Suspended",
                lastName = "User",
                email = "suspended@example.com",
                status = MemberStatus.SUSPENDED
            )
        )

        val request = mapOf(
            "memberId" to suspendedMember.id.toString(),
            "bookId" to testBook.id.toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    // ==================== GET /api/reservations/{id} ====================

    @Test
    fun `Given existing reservation When GET api reservations id Then should return 200`() {
        // Given
        val reservation = reservationRepository.save(
            Reservation(member = testMember, book = testBook, queuePosition = 1)
        )

        // When & Then
        mockMvc.perform(get("/api/reservations/${reservation.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(reservation.id.toString()))
            .andExpect(jsonPath("$.memberName").value("John Doe"))
            .andExpect(jsonPath("$.bookTitle").value("Reservation Test Book"))
            .andExpect(jsonPath("$.queuePosition").value(1))
            .andExpect(jsonPath("$.status").value("WAITING"))
    }

    @Test
    fun `Given non-existing reservation When GET api reservations id Then should return 404`() {
        // When & Then
        mockMvc.perform(get("/api/reservations/${UUID.randomUUID()}"))
            .andExpect(status().isNotFound)
    }

    // ==================== DELETE /api/reservations/{id} ====================

    @Test
    fun `Given waiting reservation When DELETE api reservations id Then should return 204`() {
        // Given
        val reservation = reservationRepository.save(
            Reservation(member = testMember, book = testBook, queuePosition = 1)
        )

        // When & Then
        mockMvc.perform(delete("/api/reservations/${reservation.id}"))
            .andExpect(status().isNoContent)

        // Verify
        val cancelled = reservationRepository.findById(reservation.id).orElse(null)
        assertNotNull(cancelled)
        assertEquals(ReservationStatus.CANCELLED, cancelled.status)
    }

    @Test
    fun `Given fulfilled reservation When DELETE api reservations id Then should return 400`() {
        // Given
        val reservation = reservationRepository.save(
            Reservation(
                member = testMember,
                book = testBook,
                queuePosition = 1,
                status = ReservationStatus.FULFILLED
            )
        )

        // When & Then
        mockMvc.perform(delete("/api/reservations/${reservation.id}"))
            .andExpect(status().isBadRequest)
    }

    // ==================== GET /api/members/{memberId}/reservations ====================

    @Test
    fun `Given member with reservations When GET api members memberId reservations Then should return list`() {
        // Given
        reservationRepository.save(
            Reservation(member = testMember, book = testBook, queuePosition = 1)
        )

        // When & Then
        mockMvc.perform(get("/api/members/${testMember.id}/reservations"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.reservations.length()").value(1))
            .andExpect(jsonPath("$.reservations[0].bookTitle").value("Reservation Test Book"))
    }

    @Test
    fun `Given non-existing member When GET api members memberId reservations Then should return 404`() {
        // When & Then
        mockMvc.perform(get("/api/members/${UUID.randomUUID()}/reservations"))
            .andExpect(status().isNotFound)
    }

    // ==================== GET /api/books/{bookId}/reservations ====================

    @Test
    fun `Given book with reservations When GET api books bookId reservations Then should return queue`() {
        // Given
        reservationRepository.save(
            Reservation(member = testMember, book = testBook, queuePosition = 1)
        )

        // When & Then
        mockMvc.perform(get("/api/books/${testBook.id}/reservations"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.bookId").value(testBook.id.toString()))
            .andExpect(jsonPath("$.bookTitle").value("Reservation Test Book"))
            .andExpect(jsonPath("$.totalWaiting").value(1))
            .andExpect(jsonPath("$.queue.length()").value(1))
    }

    @Test
    fun `Given non-existing book When GET api books bookId reservations Then should return 404`() {
        // When & Then
        mockMvc.perform(get("/api/books/${UUID.randomUUID()}/reservations"))
            .andExpect(status().isNotFound)
    }

    // ==================== Integration: Return with reservation ====================

    @Test
    fun `Given waiting reservation When copy returned Then should assign copy to reservation holder`() {
        // Given - Create loan and waiting reservation
        val borrower = memberRepository.save(
            Member(
                membershipNumber = "MBR-BORROW01",
                firstName = "Borrower",
                lastName = "User",
                email = "borrower@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val loanedCopy = bookCopyRepository.save(
            BookCopy(
                book = testBook,
                barcode = "BC-LOANED-001",
                usageType = UsageType.BOTH,
                status = CopyStatus.LOANED
            )
        )

        val loan = loanRepository.save(
            Loan(
                member = borrower,
                bookCopy = loanedCopy,
                dueDate = LocalDateTime.now().plusDays(7),
                status = LoanStatus.ACTIVE
            )
        )

        // Create waiting reservation
        val reservation = reservationRepository.save(
            Reservation(member = testMember, book = testBook, queuePosition = 1)
        )

        // When - Return the loan
        mockMvc.perform(post("/api/loans/${loan.id}/return"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.reservationAssigned").value(true))
            .andExpect(jsonPath("$.reservationId").value(reservation.id.toString()))

        // Then - Verify copy is ON_HOLD and reservation is READY_FOR_PICKUP
        val updatedCopy = bookCopyRepository.findById(loanedCopy.id).orElse(null)
        assertNotNull(updatedCopy)
        assertEquals(CopyStatus.ON_HOLD, updatedCopy.status)

        val updatedReservation = reservationRepository.findById(reservation.id).orElse(null)
        assertNotNull(updatedReservation)
        assertEquals(ReservationStatus.READY_FOR_PICKUP, updatedReservation.status)
    }

    // ==================== Integration: Borrow ON_HOLD copy ====================

    @Test
    fun `Given ON_HOLD copy and reservation holder When borrow Then should succeed and fulfill reservation`() {
        // Given
        val onHoldCopy = bookCopyRepository.save(
            BookCopy(
                book = testBook,
                barcode = "BC-ONHOLD-001",
                usageType = UsageType.BOTH,
                status = CopyStatus.ON_HOLD
            )
        )

        val reservation = reservationRepository.save(
            Reservation(
                member = testMember,
                book = testBook,
                copy = onHoldCopy,
                queuePosition = 1,
                status = ReservationStatus.READY_FOR_PICKUP,
                notifiedAt = LocalDateTime.now(),
                expiresAt = LocalDateTime.now().plusDays(3)
            )
        )

        // Create visit for member
        visitRepository.save(Visit(member = testMember))

        val borrowRequest = mapOf(
            "memberId" to testMember.id.toString(),
            "copyId" to onHoldCopy.id.toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.memberId").value(testMember.id.toString()))
            .andExpect(jsonPath("$.copyId").value(onHoldCopy.id.toString()))

        // Verify reservation is fulfilled
        val updatedReservation = reservationRepository.findById(reservation.id).orElse(null)
        assertNotNull(updatedReservation)
        assertEquals(ReservationStatus.FULFILLED, updatedReservation.status)
    }

    @Test
    fun `Given ON_HOLD copy and non-holder When borrow Then should return 400`() {
        // Given
        val holder = memberRepository.save(
            Member(
                membershipNumber = "MBR-HOLDER01",
                firstName = "Holder",
                lastName = "User",
                email = "holder@example.com",
                status = MemberStatus.ACTIVE
            )
        )

        val onHoldCopy = bookCopyRepository.save(
            BookCopy(
                book = testBook,
                barcode = "BC-ONHOLD-002",
                usageType = UsageType.BOTH,
                status = CopyStatus.ON_HOLD
            )
        )

        reservationRepository.save(
            Reservation(
                member = holder,
                book = testBook,
                copy = onHoldCopy,
                queuePosition = 1,
                status = ReservationStatus.READY_FOR_PICKUP
            )
        )

        // Create visit for testMember (not the holder)
        visitRepository.save(Visit(member = testMember))

        val borrowRequest = mapOf(
            "memberId" to testMember.id.toString(),
            "copyId" to onHoldCopy.id.toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Copy is on hold for another member's reservation"))
    }
}
