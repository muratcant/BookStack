package org.muratcant.bookstack.features.reservation.test

import org.muratcant.bookstack.features.book.domain.Book
import org.muratcant.bookstack.features.book.test.BookBuilder
import org.muratcant.bookstack.features.bookcopy.domain.BookCopy
import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.features.reservation.domain.Reservation
import org.muratcant.bookstack.features.reservation.domain.ReservationStatus
import java.time.LocalDateTime
import java.util.UUID

object ReservationBuilder {
    fun aReservation(
        id: UUID = UUID.randomUUID(),
        member: Member = MemberBuilder.anActiveMember(),
        book: Book = BookBuilder.aBook(),
        copy: BookCopy? = null,
        status: ReservationStatus = ReservationStatus.WAITING,
        queuePosition: Int = 1,
        notifiedAt: LocalDateTime? = null,
        expiresAt: LocalDateTime? = null,
        createdAt: LocalDateTime = LocalDateTime.now()
    ): Reservation = Reservation(
        id = id,
        member = member,
        book = book,
        copy = copy,
        status = status,
        queuePosition = queuePosition,
        notifiedAt = notifiedAt,
        expiresAt = expiresAt,
        createdAt = createdAt
    )

    fun aWaitingReservation(
        id: UUID = UUID.randomUUID(),
        member: Member = MemberBuilder.anActiveMember(),
        book: Book = BookBuilder.aBook(),
        queuePosition: Int = 1
    ) = aReservation(
        id = id,
        member = member,
        book = book,
        status = ReservationStatus.WAITING,
        queuePosition = queuePosition
    )

    fun aReadyForPickupReservation(
        id: UUID = UUID.randomUUID(),
        member: Member = MemberBuilder.anActiveMember(),
        book: Book = BookBuilder.aBook(),
        copy: BookCopy,
        queuePosition: Int = 1
    ) = aReservation(
        id = id,
        member = member,
        book = book,
        copy = copy,
        status = ReservationStatus.READY_FOR_PICKUP,
        queuePosition = queuePosition,
        notifiedAt = LocalDateTime.now(),
        expiresAt = LocalDateTime.now().plusDays(3)
    )
}
