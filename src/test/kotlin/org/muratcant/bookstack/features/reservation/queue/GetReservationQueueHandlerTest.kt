package org.muratcant.bookstack.features.reservation.queue

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.book.test.BookBuilder
import org.muratcant.bookstack.features.bookcopy.test.BookCopyBuilder
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import org.muratcant.bookstack.features.reservation.test.ReservationBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class GetReservationQueueHandlerTest : FunSpec({

    val reservationRepository = mockk<ReservationRepository>()
    val bookRepository = mockk<BookRepository>()
    val handler = GetReservationQueueHandler(reservationRepository, bookRepository)

    beforeTest {
        clearMocks(reservationRepository, bookRepository)
    }

    test("Given book with 3 waiting reservations When get queue Then should return ordered by queue position") {
        val bookId = UUID.randomUUID()
        val book = BookBuilder.aBook(id = bookId)
        val reservation1 = ReservationBuilder.aWaitingReservation(book = book, queuePosition = 1)
        val reservation2 = ReservationBuilder.aWaitingReservation(book = book, queuePosition = 2)
        val reservation3 = ReservationBuilder.aWaitingReservation(book = book, queuePosition = 3)

        every { bookRepository.findById(bookId) } returns Optional.of(book)
        every {
            reservationRepository.findByBookIdAndStatusInOrderByQueuePositionAsc(
                bookId,
                listOf(ReservationStatus.WAITING, ReservationStatus.READY_FOR_PICKUP)
            )
        } returns listOf(reservation1, reservation2, reservation3)

        val response = handler.handle(bookId)

        response.bookId shouldBe bookId
        response.bookTitle shouldBe book.title
        response.totalWaiting shouldBe 3
        response.queue.size shouldBe 3
        response.queue[0].queuePosition shouldBe 1
        response.queue[1].queuePosition shouldBe 2
        response.queue[2].queuePosition shouldBe 3
    }

    test("Given book with mixed status reservations When get queue Then should only include active statuses") {
        val bookId = UUID.randomUUID()
        val book = BookBuilder.aBook(id = bookId)
        val copy = BookCopyBuilder.anAvailableCopy()
        val waitingReservation = ReservationBuilder.aWaitingReservation(book = book, queuePosition = 1)
        val readyReservation = ReservationBuilder.aReadyForPickupReservation(book = book, copy = copy, queuePosition = 2)

        every { bookRepository.findById(bookId) } returns Optional.of(book)
        every {
            reservationRepository.findByBookIdAndStatusInOrderByQueuePositionAsc(
                bookId,
                listOf(ReservationStatus.WAITING, ReservationStatus.READY_FOR_PICKUP)
            )
        } returns listOf(waitingReservation, readyReservation)

        val response = handler.handle(bookId)

        response.queue.size shouldBe 2
        response.queue[0].status shouldBe "WAITING"
        response.queue[1].status shouldBe "READY_FOR_PICKUP"
    }

    test("Given book with no reservations When get queue Then should return empty queue") {
        val bookId = UUID.randomUUID()
        val book = BookBuilder.aBook(id = bookId)

        every { bookRepository.findById(bookId) } returns Optional.of(book)
        every {
            reservationRepository.findByBookIdAndStatusInOrderByQueuePositionAsc(
                bookId,
                listOf(ReservationStatus.WAITING, ReservationStatus.READY_FOR_PICKUP)
            )
        } returns emptyList()

        val response = handler.handle(bookId)

        response.totalWaiting shouldBe 0
        response.queue.size shouldBe 0
    }

    test("Given non-existent book When get queue Then should throw ResourceNotFoundException") {
        val bookId = UUID.randomUUID()

        every { bookRepository.findById(bookId) } returns Optional.empty()

        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(bookId)
        }

        exception.message shouldBe "Book not found: $bookId"
    }

    test("Given book with 2 waiting and 1 ready for pickup When get queue Then should count only waiting") {
        val bookId = UUID.randomUUID()
        val book = BookBuilder.aBook(id = bookId)
        val copy = BookCopyBuilder.anAvailableCopy()
        val waiting1 = ReservationBuilder.aWaitingReservation(book = book, queuePosition = 1)
        val waiting2 = ReservationBuilder.aWaitingReservation(book = book, queuePosition = 2)
        val ready = ReservationBuilder.aReadyForPickupReservation(book = book, copy = copy, queuePosition = 3)

        every { bookRepository.findById(bookId) } returns Optional.of(book)
        every {
            reservationRepository.findByBookIdAndStatusInOrderByQueuePositionAsc(
                bookId,
                listOf(ReservationStatus.WAITING, ReservationStatus.READY_FOR_PICKUP)
            )
        } returns listOf(waiting1, waiting2, ready)

        val response = handler.handle(bookId)

        response.totalWaiting shouldBe 2
        response.queue.size shouldBe 3
    }
})
