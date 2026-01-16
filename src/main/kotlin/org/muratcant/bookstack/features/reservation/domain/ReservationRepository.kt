package org.muratcant.bookstack.features.reservation.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface ReservationRepository : JpaRepository<Reservation, UUID> {
    fun findByMemberIdAndStatusInOrderByCreatedAtDesc(
        memberId: UUID,
        statuses: List<ReservationStatus>
    ): List<Reservation>

    fun findByBookIdAndStatusOrderByQueuePositionAsc(
        bookId: UUID,
        status: ReservationStatus
    ): List<Reservation>

    fun findByBookIdAndStatusInOrderByQueuePositionAsc(
        bookId: UUID,
        statuses: List<ReservationStatus>
    ): List<Reservation>

    fun existsByMemberIdAndBookIdAndStatusIn(
        memberId: UUID,
        bookId: UUID,
        statuses: List<ReservationStatus>
    ): Boolean

    fun findFirstByBookIdAndStatusOrderByQueuePositionAsc(
        bookId: UUID,
        status: ReservationStatus
    ): Reservation?

    fun countByBookIdAndStatus(bookId: UUID, status: ReservationStatus): Int

    @Modifying
    @Query("""
        UPDATE Reservation r 
        SET r.queuePosition = r.queuePosition - 1 
        WHERE r.book.id = :bookId 
        AND r.status = 'WAITING' 
        AND r.queuePosition > :cancelledPosition
    """)
    fun decrementQueuePositionsAfter(bookId: UUID, cancelledPosition: Int)

    fun findByStatusAndExpiresAtBefore(
        status: ReservationStatus,
        expiresAt: LocalDateTime
    ): List<Reservation>

    fun findByCopyIdAndStatus(copyId: UUID, status: ReservationStatus): Reservation?
}
