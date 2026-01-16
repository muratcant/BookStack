package org.muratcant.bookstack.features.reservation.list

import org.muratcant.bookstack.features.reservation.domain.Reservation
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListReservationsHandler(
    private val reservationRepository: ReservationRepository
) {
    @Transactional(readOnly = true)
    fun handle(): ListReservationsResponse {
        val reservations = reservationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))

        return ListReservationsResponse(
            reservations = reservations.map { it.toItem() }
        )
    }

    private fun Reservation.toItem() = ReservationListItem(
        id = id,
        memberId = member.id,
        memberName = "${member.firstName} ${member.lastName}",
        membershipNumber = member.membershipNumber,
        bookId = book.id,
        bookTitle = book.title,
        queuePosition = queuePosition,
        status = status.name,
        expiresAt = expiresAt,
        createdAt = createdAt
    )
}
