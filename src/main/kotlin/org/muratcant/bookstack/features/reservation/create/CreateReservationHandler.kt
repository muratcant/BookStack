package org.muratcant.bookstack.features.reservation.create

import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.domain.MemberStatus
import org.muratcant.bookstack.features.reservation.domain.Reservation
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import org.muratcant.bookstack.shared.exception.DuplicateResourceException
import org.muratcant.bookstack.shared.exception.MemberNotActiveException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateReservationHandler(
    private val reservationRepository: ReservationRepository,
    private val memberRepository: MemberRepository,
    private val bookRepository: BookRepository
) {
    @Transactional
    fun handle(request: CreateReservationRequest): CreateReservationResponse {
        val member = memberRepository.findById(request.memberId)
            .orElseThrow { ResourceNotFoundException("Member not found: ${request.memberId}") }

        val book = bookRepository.findById(request.bookId)
            .orElseThrow { ResourceNotFoundException("Book not found: ${request.bookId}") }

        if (member.status != MemberStatus.ACTIVE) {
            throw MemberNotActiveException("Member is not active: ${member.status}")
        }

        val activeStatuses = listOf(ReservationStatus.WAITING, ReservationStatus.READY_FOR_PICKUP)
        if (reservationRepository.existsByMemberIdAndBookIdAndStatusIn(
                request.memberId, request.bookId, activeStatuses
            )) {
            throw DuplicateResourceException("Member already has an active reservation for this book")
        }

        val currentQueueSize = reservationRepository.countByBookIdAndStatus(
            request.bookId, ReservationStatus.WAITING
        )
        val queuePosition = currentQueueSize + 1

        val reservation = Reservation(
            member = member,
            book = book,
            queuePosition = queuePosition
        )

        val savedReservation = reservationRepository.save(reservation)
        return savedReservation.toResponse()
    }

    private fun Reservation.toResponse() = CreateReservationResponse(
        id = id,
        memberId = member.id,
        memberName = "${member.firstName} ${member.lastName}",
        bookId = book.id,
        bookTitle = book.title,
        queuePosition = queuePosition,
        status = status.name,
        createdAt = createdAt
    )
}
