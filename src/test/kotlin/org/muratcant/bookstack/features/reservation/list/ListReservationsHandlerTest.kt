package org.muratcant.bookstack.features.reservation.list

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.bookcopy.test.BookCopyBuilder
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import org.muratcant.bookstack.features.reservation.test.ReservationBuilder
import org.springframework.data.domain.Sort

class ListReservationsHandlerTest : FunSpec({

    val reservationRepository = mockk<ReservationRepository>()
    val handler = ListReservationsHandler(reservationRepository)

    beforeTest {
        clearMocks(reservationRepository)
    }

    test("Given reservations exist When list all Then should return all ordered by createdAt desc") {
        val copy = BookCopyBuilder.anAvailableCopy()
        val waiting = ReservationBuilder.aWaitingReservation(queuePosition = 1)
        val ready = ReservationBuilder.aReadyForPickupReservation(copy = copy, queuePosition = 2)
        val fulfilled = ReservationBuilder.aReservation(status = ReservationStatus.FULFILLED, queuePosition = 0)
        val cancelled = ReservationBuilder.aReservation(status = ReservationStatus.CANCELLED, queuePosition = 0)

        every {
            reservationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
        } returns listOf(waiting, ready, fulfilled, cancelled)

        val response = handler.handle()

        response.reservations.size shouldBe 4
        response.reservations[0].status shouldBe "WAITING"
        response.reservations[1].status shouldBe "READY_FOR_PICKUP"
        response.reservations[2].status shouldBe "FULFILLED"
        response.reservations[3].status shouldBe "CANCELLED"
    }

    test("Given multiple reservations When list Then should include all details") {
        val copy = BookCopyBuilder.anAvailableCopy()
        val reservation1 = ReservationBuilder.aWaitingReservation(queuePosition = 1)
        val reservation2 = ReservationBuilder.aReadyForPickupReservation(copy = copy, queuePosition = 2)

        every {
            reservationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
        } returns listOf(reservation1, reservation2)

        val response = handler.handle()

        response.reservations.size shouldBe 2
        response.reservations[0].memberId shouldBe reservation1.member.id
        response.reservations[0].memberName shouldBe "${reservation1.member.firstName} ${reservation1.member.lastName}"
        response.reservations[0].membershipNumber shouldBe reservation1.member.membershipNumber
        response.reservations[0].bookId shouldBe reservation1.book.id
        response.reservations[0].bookTitle shouldBe reservation1.book.title
        response.reservations[0].queuePosition shouldBe 1
        response.reservations[0].status shouldBe "WAITING"
        response.reservations[0].expiresAt shouldBe null
        response.reservations[0].createdAt shouldNotBe null
        
        response.reservations[1].queuePosition shouldBe 2
        response.reservations[1].status shouldBe "READY_FOR_PICKUP"
        response.reservations[1].expiresAt shouldNotBe null
    }

    test("Given no reservations When list Then should return empty list") {
        every {
            reservationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
        } returns emptyList()

        val response = handler.handle()

        response.reservations.size shouldBe 0
    }
})
