package org.muratcant.bookstack.features.reservation.process

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.bookcopy.test.BookCopyBuilder
import org.muratcant.bookstack.features.reservation.config.ReservationProperties
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import org.muratcant.bookstack.features.reservation.test.ReservationBuilder
import java.util.UUID

class ProcessReservationServiceTest : FunSpec({

    val reservationRepository = mockk<ReservationRepository>()
    val bookCopyRepository = mockk<BookCopyRepository>()
    val reservationProperties = ReservationProperties(pickupWindowDays = 3)
    val service = ProcessReservationService(reservationRepository, bookCopyRepository, reservationProperties)

    beforeTest {
        clearMocks(reservationRepository, bookCopyRepository)
    }

    test("Given waiting reservation When processAfterReturn Then should assign copy and mark ON_HOLD") {
        // Given
        val copy = BookCopyBuilder.anAvailableCopy()
        val reservation = ReservationBuilder.aWaitingReservation(book = copy.book)

        every { reservationRepository.findFirstByBookIdAndStatusOrderByQueuePositionAsc(
            copy.book.id, ReservationStatus.WAITING
        ) } returns reservation
        every { reservationRepository.save(any()) } answers { firstArg() }
        every { bookCopyRepository.save(any()) } answers { firstArg() }

        // When
        val result = service.processAfterReturn(copy)

        // Then
        result shouldNotBe null
        result!!.status shouldBe ReservationStatus.READY_FOR_PICKUP
        result.copy shouldBe copy
        result.notifiedAt shouldNotBe null
        result.expiresAt shouldNotBe null
        copy.status shouldBe CopyStatus.ON_HOLD
        verify(exactly = 1) { reservationRepository.save(any()) }
        verify(exactly = 1) { bookCopyRepository.save(copy) }
    }

    test("Given no waiting reservation When processAfterReturn Then should return null") {
        // Given
        val copy = BookCopyBuilder.anAvailableCopy()

        every { reservationRepository.findFirstByBookIdAndStatusOrderByQueuePositionAsc(
            copy.book.id, ReservationStatus.WAITING
        ) } returns null

        // When
        val result = service.processAfterReturn(copy)

        // Then
        result shouldBe null
        verify(exactly = 0) { reservationRepository.save(any()) }
    }

    test("Given ready-for-pickup reservation When fulfillReservation Then should mark as fulfilled") {
        // Given
        val copyId = UUID.randomUUID()
        val reservation = ReservationBuilder.aReservation(status = ReservationStatus.READY_FOR_PICKUP)

        every { reservationRepository.findByCopyIdAndStatus(
            copyId, ReservationStatus.READY_FOR_PICKUP
        ) } returns reservation
        every { reservationRepository.save(any()) } answers { firstArg() }

        // When
        val result = service.fulfillReservation(copyId)

        // Then
        result shouldNotBe null
        result!!.status shouldBe ReservationStatus.FULFILLED
        verify(exactly = 1) { reservationRepository.save(any()) }
    }

    test("Given no ready-for-pickup reservation When fulfillReservation Then should return null") {
        // Given
        val copyId = UUID.randomUUID()

        every { reservationRepository.findByCopyIdAndStatus(
            copyId, ReservationStatus.READY_FOR_PICKUP
        ) } returns null

        // When
        val result = service.fulfillReservation(copyId)

        // Then
        result shouldBe null
        verify(exactly = 0) { reservationRepository.save(any()) }
    }
})
