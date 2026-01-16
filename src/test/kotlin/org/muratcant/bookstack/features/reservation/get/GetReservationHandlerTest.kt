package org.muratcant.bookstack.features.reservation.get

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.bookcopy.test.BookCopyBuilder
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.test.ReservationBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.Optional
import java.util.UUID

class GetReservationHandlerTest : FunSpec({

    val reservationRepository = mockk<ReservationRepository>()
    val handler = GetReservationHandler(reservationRepository)

    beforeTest {
        clearMocks(reservationRepository)
    }

    test("Given waiting reservation When get Then should return reservation without copy") {
        val reservationId = UUID.randomUUID()
        val reservation = ReservationBuilder.aWaitingReservation(id = reservationId)

        every { reservationRepository.findById(reservationId) } returns Optional.of(reservation)

        val response = handler.handle(reservationId)

        response.id shouldBe reservationId
        response.copyId shouldBe null
        response.barcode shouldBe null
        response.status shouldBe "WAITING"
    }

    test("Given ready for pickup reservation When get Then should include assigned copy") {
        val reservationId = UUID.randomUUID()
        val copy = BookCopyBuilder.anAvailableCopy()
        val reservation = ReservationBuilder.aReadyForPickupReservation(id = reservationId, copy = copy)

        every { reservationRepository.findById(reservationId) } returns Optional.of(reservation)

        val response = handler.handle(reservationId)

        response.id shouldBe reservationId
        response.copyId shouldBe copy.id
        response.barcode shouldBe copy.barcode
        response.status shouldBe "READY_FOR_PICKUP"
    }

    test("Given reservation with expiry When get Then should include expiry timestamp") {
        val reservationId = UUID.randomUUID()
        val copy = BookCopyBuilder.anAvailableCopy()
        val reservation = ReservationBuilder.aReadyForPickupReservation(id = reservationId, copy = copy)

        every { reservationRepository.findById(reservationId) } returns Optional.of(reservation)

        val response = handler.handle(reservationId)

        response.id shouldBe reservationId
        response.notifiedAt shouldNotBe null
        response.expiresAt shouldNotBe null
    }

    test("Given non-existent reservation When get Then should throw ResourceNotFoundException") {
        val reservationId = UUID.randomUUID()

        every { reservationRepository.findById(reservationId) } returns Optional.empty()

        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(reservationId)
        }

        exception.message shouldBe "Reservation not found: $reservationId"
    }

    test("Given reservation When get Then should include all details") {
        val reservationId = UUID.randomUUID()
        val reservation = ReservationBuilder.aWaitingReservation(id = reservationId, queuePosition = 3)

        every { reservationRepository.findById(reservationId) } returns Optional.of(reservation)

        val response = handler.handle(reservationId)

        response.memberId shouldBe reservation.member.id
        response.memberName shouldBe "${reservation.member.firstName} ${reservation.member.lastName}"
        response.bookId shouldBe reservation.book.id
        response.bookTitle shouldBe reservation.book.title
        response.queuePosition shouldBe 3
        response.createdAt shouldNotBe null
    }
})
