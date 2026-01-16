package org.muratcant.bookstack.features.reservation.process

import org.muratcant.bookstack.features.bookcopy.domain.BookCopy
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.reservation.config.ReservationProperties
import org.muratcant.bookstack.features.reservation.domain.Reservation
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProcessReservationService(
    private val reservationRepository: ReservationRepository,
    private val bookCopyRepository: BookCopyRepository,
    private val reservationProperties: ReservationProperties
) {
    /**
     * Process reservation queue after a copy is returned.
     * If there's a waiting reservation for the book, assigns the copy and marks it ON_HOLD.
     * @return The reservation that was processed, or null if no reservation was waiting.
     */
    @Transactional
    fun processAfterReturn(copy: BookCopy): Reservation? {
        val bookId = copy.book.id

        val nextReservation = reservationRepository.findFirstByBookIdAndStatusOrderByQueuePositionAsc(
            bookId, ReservationStatus.WAITING
        ) ?: return null

        // Assign copy to reservation
        nextReservation.markReadyForPickup(copy, reservationProperties.pickupWindowDays)

        // Update copy status to ON_HOLD
        copy.status = CopyStatus.ON_HOLD
        bookCopyRepository.save(copy)

        return reservationRepository.save(nextReservation)
    }

    /**
     * Fulfill a reservation when the holder borrows the assigned copy.
     */
    @Transactional
    fun fulfillReservation(copyId: UUID): Reservation? {
        val reservation = reservationRepository.findByCopyIdAndStatus(
            copyId, ReservationStatus.READY_FOR_PICKUP
        ) ?: return null

        reservation.fulfill()
        return reservationRepository.save(reservation)
    }
}
