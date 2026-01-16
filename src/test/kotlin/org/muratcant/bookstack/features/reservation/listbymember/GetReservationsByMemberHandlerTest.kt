package org.muratcant.bookstack.features.reservation.listbymember

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.muratcant.bookstack.features.bookcopy.test.BookCopyBuilder
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import org.muratcant.bookstack.features.reservation.test.ReservationBuilder
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import java.util.UUID

class GetReservationsByMemberHandlerTest : FunSpec({

    val reservationRepository = mockk<ReservationRepository>()
    val memberRepository = mockk<MemberRepository>()
    val handler = GetReservationsByMemberHandler(reservationRepository, memberRepository)

    beforeTest {
        clearMocks(reservationRepository, memberRepository)
    }

    test("Given member with active reservations When get by member Then should return only active ones") {
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val copy = BookCopyBuilder.anAvailableCopy()
        val waiting1 = ReservationBuilder.aWaitingReservation(member = member, queuePosition = 1)
        val waiting2 = ReservationBuilder.aWaitingReservation(member = member, queuePosition = 2)
        val ready = ReservationBuilder.aReadyForPickupReservation(member = member, copy = copy, queuePosition = 3)

        every { memberRepository.existsById(memberId) } returns true
        every {
            reservationRepository.findByMemberIdAndStatusInOrderByCreatedAtDesc(
                memberId,
                listOf(ReservationStatus.WAITING, ReservationStatus.READY_FOR_PICKUP)
            )
        } returns listOf(waiting1, waiting2, ready)

        val response = handler.handle(memberId)

        response.reservations.size shouldBe 3
        response.reservations[0].status shouldBe "WAITING"
        response.reservations[1].status shouldBe "WAITING"
        response.reservations[2].status shouldBe "READY_FOR_PICKUP"
    }

    test("Given member with no active reservations When get by member Then should return empty list") {
        val memberId = UUID.randomUUID()

        every { memberRepository.existsById(memberId) } returns true
        every {
            reservationRepository.findByMemberIdAndStatusInOrderByCreatedAtDesc(
                memberId,
                listOf(ReservationStatus.WAITING, ReservationStatus.READY_FOR_PICKUP)
            )
        } returns emptyList()

        val response = handler.handle(memberId)

        response.reservations.size shouldBe 0
    }

    test("Given non-existent member When get reservations Then should throw ResourceNotFoundException") {
        val memberId = UUID.randomUUID()

        every { memberRepository.existsById(memberId) } returns false

        val exception = shouldThrow<ResourceNotFoundException> {
            handler.handle(memberId)
        }

        exception.message shouldBe "Member not found: $memberId"
    }

    test("Given member with reservations When get Then should include all details") {
        val memberId = UUID.randomUUID()
        val member = MemberBuilder.anActiveMember(id = memberId)
        val copy = BookCopyBuilder.anAvailableCopy()
        val reservation = ReservationBuilder.aReadyForPickupReservation(member = member, copy = copy, queuePosition = 1)

        every { memberRepository.existsById(memberId) } returns true
        every {
            reservationRepository.findByMemberIdAndStatusInOrderByCreatedAtDesc(
                memberId,
                listOf(ReservationStatus.WAITING, ReservationStatus.READY_FOR_PICKUP)
            )
        } returns listOf(reservation)

        val response = handler.handle(memberId)

        response.reservations.size shouldBe 1
        response.reservations[0].bookId shouldBe reservation.book.id
        response.reservations[0].bookTitle shouldBe reservation.book.title
        response.reservations[0].isbn shouldBe reservation.book.isbn
        response.reservations[0].queuePosition shouldBe 1
        response.reservations[0].status shouldBe "READY_FOR_PICKUP"
        response.reservations[0].expiresAt shouldNotBe null
        response.reservations[0].createdAt shouldNotBe null
    }
})
