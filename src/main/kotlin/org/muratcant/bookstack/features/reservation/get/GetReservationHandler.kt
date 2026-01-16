package org.muratcant.bookstack.features.reservation.get

import org.muratcant.bookstack.features.reservation.domain.Reservation
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetReservationHandler(
    private val reservationRepository: ReservationRepository
) {
    @Transactional(readOnly = true)
    fun handle(reservationId: UUID): GetReservationResponse {
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { ResourceNotFoundException("Reservation not found: $reservationId") }

        return reservation.toResponse()
    }

    private fun Reservation.toResponse() = GetReservationResponse(
        id = id,
        memberId = member.id,
        memberName = "${member.firstName} ${member.lastName}",
        bookId = book.id,
        bookTitle = book.title,
        copyId = copy?.id,
        barcode = copy?.barcode,
        queuePosition = queuePosition,
        status = status.name,
        notifiedAt = notifiedAt,
        expiresAt = expiresAt,
        createdAt = createdAt
    )
}
