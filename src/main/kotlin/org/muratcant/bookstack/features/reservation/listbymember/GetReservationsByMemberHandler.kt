package org.muratcant.bookstack.features.reservation.listbymember

import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.reservation.domain.Reservation
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetReservationsByMemberHandler(
    private val reservationRepository: ReservationRepository,
    private val memberRepository: MemberRepository
) {
    @Transactional(readOnly = true)
    fun handle(memberId: UUID): GetReservationsByMemberResponse {
        if (!memberRepository.existsById(memberId)) {
            throw ResourceNotFoundException("Member not found: $memberId")
        }

        val activeStatuses = listOf(
            ReservationStatus.WAITING,
            ReservationStatus.READY_FOR_PICKUP
        )

        val reservations = reservationRepository.findByMemberIdAndStatusInOrderByCreatedAtDesc(
            memberId, activeStatuses
        )

        return GetReservationsByMemberResponse(
            reservations = reservations.map { it.toItem() }
        )
    }

    private fun Reservation.toItem() = MemberReservationItem(
        id = id,
        bookId = book.id,
        bookTitle = book.title,
        isbn = book.isbn,
        queuePosition = queuePosition,
        status = status.name,
        expiresAt = expiresAt,
        createdAt = createdAt
    )
}
