package org.muratcant.bookstack.features.reservation.domain

import jakarta.persistence.*
import org.muratcant.bookstack.features.book.domain.Book
import org.muratcant.bookstack.features.bookcopy.domain.BookCopy
import org.muratcant.bookstack.features.member.domain.Member
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "reservations")
class Reservation(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    val book: Book,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "copy_id", nullable = true)
    var copy: BookCopy? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ReservationStatus = ReservationStatus.WAITING,

    @Column(nullable = false)
    var queuePosition: Int,

    @Column(nullable = true)
    var notifiedAt: LocalDateTime? = null,

    @Column(nullable = true)
    var expiresAt: LocalDateTime? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun markReadyForPickup(copy: BookCopy, pickupWindowDays: Int) {
        this.copy = copy
        this.status = ReservationStatus.READY_FOR_PICKUP
        this.notifiedAt = LocalDateTime.now()
        this.expiresAt = LocalDateTime.now().plusDays(pickupWindowDays.toLong())
    }

    fun fulfill() {
        status = ReservationStatus.FULFILLED
    }

    fun expire() {
        status = ReservationStatus.EXPIRED
        copy = null
    }

    fun cancel() {
        status = ReservationStatus.CANCELLED
        copy = null
    }

    fun isWaiting(): Boolean = status == ReservationStatus.WAITING
    fun isReadyForPickup(): Boolean = status == ReservationStatus.READY_FOR_PICKUP
    fun isActive(): Boolean = status == ReservationStatus.WAITING || status == ReservationStatus.READY_FOR_PICKUP
}
