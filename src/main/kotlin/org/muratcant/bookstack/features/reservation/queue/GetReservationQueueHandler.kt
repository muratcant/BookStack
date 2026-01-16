package org.muratcant.bookstack.features.reservation.queue

import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.reservation.domain.Reservation
import org.muratcant.bookstack.features.reservation.domain.ReservationRepository
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetReservationQueueHandler(
    private val reservationRepository: ReservationRepository,
    private val bookRepository: BookRepository
) {
    @Transactional(readOnly = true)
    fun handle(bookId: UUID): GetReservationQueueResponse {
        val book = bookRepository.findById(bookId)
            .orElseThrow { ResourceNotFoundException("Book not found: $bookId") }

        val activeStatuses = listOf(
            ReservationStatus.WAITING,
            ReservationStatus.READY_FOR_PICKUP
        )

        val reservations = reservationRepository.findByBookIdAndStatusInOrderByQueuePositionAsc(
            bookId, activeStatuses
        )

        val waitingCount = reservations.count { it.status == ReservationStatus.WAITING }

        return GetReservationQueueResponse(
            bookId = book.id,
            bookTitle = book.title,
            totalWaiting = waitingCount,
            queue = reservations.map { it.toItem() }
        )
    }

    private fun Reservation.toItem() = ReservationQueueItem(
        id = id,
        memberId = member.id,
        memberName = "${member.firstName} ${member.lastName}",
        membershipNumber = member.membershipNumber,
        queuePosition = queuePosition,
        status = status.name,
        expiresAt = expiresAt,
        createdAt = createdAt
    )
}
