package org.muratcant.bookstack.features.reservation.cancel

import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import org.muratcant.bookstack.shared.exception.BusinessRuleException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CancelReservationHandler(
    private val reservationRepository: ReservationRepository,
    private val bookCopyRepository: BookCopyRepository
) {
    @Transactional
    fun handle(reservationId: UUID) {
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { ResourceNotFoundException("Reservation not found: $reservationId") }

        if (!reservation.isActive()) {
            throw BusinessRuleException("Reservation cannot be cancelled: ${reservation.status}")
        }

        val bookId = reservation.book.id
        val cancelledPosition = reservation.queuePosition

        // If reservation had a copy assigned (READY_FOR_PICKUP), release it
        reservation.copy?.let { copy ->
            copy.status = CopyStatus.AVAILABLE
            bookCopyRepository.save(copy)
        }

        reservation.cancel()
        reservationRepository.save(reservation)

        // Decrement queue positions for waiting reservations after cancelled one
        if (reservation.status == ReservationStatus.CANCELLED) {
            reservationRepository.decrementQueuePositionsAfter(bookId, cancelledPosition)
        }
    }
}
