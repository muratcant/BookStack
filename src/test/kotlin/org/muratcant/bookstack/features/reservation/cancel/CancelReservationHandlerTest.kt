package org.muratcant.bookstack.features.reservation.cancel

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.bookcopy.test.BookCopyBuilder
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import org.muratcant.bookstack.features.reservation.test.ReservationBuilder
import org.muratcant.bookstack.shared.exception.BusinessRuleException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class CancelReservationHandlerTest : FunSpec({

    val reservationRepository = mockk<ReservationRepository>()
    val bookCopyRepository = mockk<BookCopyRepository>()
    val handler = CancelReservationHandler(reservationRepository, bookCopyRepository)

    beforeTest {
        clearMocks(reservationRepository, bookCopyRepository)
    }

    test("Given waiting reservation When cancel Then should mark as cancelled and update queue") {
        // Given
        val reservationId = UUID.randomUUID()
        val bookId = UUID.randomUUID()
        val reservation = ReservationBuilder.aWaitingReservation(id = reservationId, queuePosition = 2)

        every { reservationRepository.findById(reservationId) } returns Optional.of(reservation)
        every { reservationRepository.save(any()) } answers { firstArg() }
        every { reservationRepository.decrementQueuePositionsAfter(any(), any()) } returns Unit

        // When
        handler.handle(reservationId)

        // Then
        reservation.status shouldBe ReservationStatus.CANCELLED
        verify(exactly = 1) { reservationRepository.save(any()) }
        verify(exactly = 1) { reservationRepository.decrementQueuePositionsAfter(any(), 2) }
    }

    test("Given ready-for-pickup reservation When cancel Then should release copy") {
        // Given
        val reservationId = UUID.randomUUID()
        val copy = BookCopyBuilder.anOnHoldCopy()
        val reservation = ReservationBuilder.aReadyForPickupReservation(
            id = reservationId,
            copy = copy,
            queuePosition = 1
        )

        every { reservationRepository.findById(reservationId) } returns Optional.of(reservation)
        every { reservationRepository.save(any()) } answers { firstArg() }
        every { bookCopyRepository.save(any()) } answers { firstArg() }
        every { reservationRepository.decrementQueuePositionsAfter(any(), any()) } returns Unit

        // When
        handler.handle(reservationId)

        // Then
        reservation.status shouldBe ReservationStatus.CANCELLED
        copy.status shouldBe CopyStatus.AVAILABLE
        verify(exactly = 1) { bookCopyRepository.save(copy) }
    }

    test("Given already fulfilled reservation When cancel Then should throw BusinessRuleException") {
        // Given
        val reservationId = UUID.randomUUID()
        val reservation = ReservationBuilder.aReservation(
            id = reservationId,
            status = ReservationStatus.FULFILLED
        )

        every { reservationRepository.findById(reservationId) } returns Optional.of(reservation)

        // When & Then
        shouldThrow<BusinessRuleException> {
            handler.handle(reservationId)
        }
        verify(exactly = 0) { reservationRepository.save(any()) }
    }

    test("Given non-existing reservation When cancel Then should throw ResourceNotFoundException") {
        // Given
        val reservationId = UUID.randomUUID()

        every { reservationRepository.findById(reservationId) } returns Optional.empty()

        // When & Then
        shouldThrow<ResourceNotFoundException> {
            handler.handle(reservationId)
        }
    }
})
