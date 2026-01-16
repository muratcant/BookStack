package org.muratcant.bookstack.features.reservation.create

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.book.test.BookBuilder
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import org.muratcant.bookstack.shared.exception.DuplicateResourceException
import org.muratcant.bookstack.shared.exception.MemberNotActiveException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class CreateReservationHandlerTest : FunSpec({

    val reservationRepository = mockk<ReservationRepository>()
    val memberRepository = mockk<MemberRepository>()
    val bookRepository = mockk<BookRepository>()
    val handler = CreateReservationHandler(reservationRepository, memberRepository, bookRepository)

    beforeTest {
        clearMocks(reservationRepository, memberRepository, bookRepository)
    }

    test("Given active member and book When create reservation Then should create with correct queue position") {
        // Given
        val memberId = UUID.randomUUID()
        val bookId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val book = BookBuilder.aBook(id = bookId)
        val request = CreateReservationRequest(memberId = memberId, bookId = bookId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookRepository.findById(bookId) } returns Optional.of(book)
        every { reservationRepository.existsByMemberIdAndBookIdAndStatusIn(memberId, bookId, any()) } returns false
        every { reservationRepository.countByBookIdAndStatus(bookId, ReservationStatus.WAITING) } returns 2
        every { reservationRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(request)

        // Then
        response.id shouldNotBe null
        response.memberId shouldBe memberId
        response.bookId shouldBe bookId
        response.queuePosition shouldBe 3
        response.status shouldBe "WAITING"
        verify(exactly = 1) { reservationRepository.save(any()) }
    }

    test("Given first reservation for book When create Then should have queue position 1") {
        // Given
        val memberId = UUID.randomUUID()
        val bookId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val book = BookBuilder.aBook(id = bookId)
        val request = CreateReservationRequest(memberId = memberId, bookId = bookId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookRepository.findById(bookId) } returns Optional.of(book)
        every { reservationRepository.existsByMemberIdAndBookIdAndStatusIn(memberId, bookId, any()) } returns false
        every { reservationRepository.countByBookIdAndStatus(bookId, ReservationStatus.WAITING) } returns 0
        every { reservationRepository.save(any()) } answers { firstArg() }

        // When
        val response = handler.handle(request)

        // Then
        response.queuePosition shouldBe 1
    }

    test("Given suspended member When create reservation Then should throw MemberNotActiveException") {
        // Given
        val memberId = UUID.randomUUID()
        val bookId = UUID.randomUUID()
        val member = MemberBuilder.aSuspendedMember(id = memberId)
        val book = BookBuilder.aBook(id = bookId)
        val request = CreateReservationRequest(memberId = memberId, bookId = bookId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookRepository.findById(bookId) } returns Optional.of(book)

        // When & Then
        shouldThrow<MemberNotActiveException> {
            handler.handle(request)
        }
        verify(exactly = 0) { reservationRepository.save(any()) }
    }

    test("Given member already has active reservation When create Then should throw DuplicateResourceException") {
        // Given
        val memberId = UUID.randomUUID()
        val bookId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val book = BookBuilder.aBook(id = bookId)
        val request = CreateReservationRequest(memberId = memberId, bookId = bookId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookRepository.findById(bookId) } returns Optional.of(book)
        every { reservationRepository.existsByMemberIdAndBookIdAndStatusIn(memberId, bookId, any()) } returns true

        // When & Then
        shouldThrow<DuplicateResourceException> {
            handler.handle(request)
        }
        verify(exactly = 0) { reservationRepository.save(any()) }
    }

    test("Given non-existing member When create reservation Then should throw ResourceNotFoundException") {
        // Given
        val memberId = UUID.randomUUID()
        val bookId = UUID.randomUUID()
        val request = CreateReservationRequest(memberId = memberId, bookId = bookId)

        every { memberRepository.findById(memberId) } returns Optional.empty()

        // When & Then
        shouldThrow<ResourceNotFoundException> {
            handler.handle(request)
        }
    }

    test("Given non-existing book When create reservation Then should throw ResourceNotFoundException") {
        // Given
        val memberId = UUID.randomUUID()
        val bookId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val request = CreateReservationRequest(memberId = memberId, bookId = bookId)

        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { bookRepository.findById(bookId) } returns Optional.empty()

        // When & Then
        shouldThrow<ResourceNotFoundException> {
            handler.handle(request)
        }
    }
})
