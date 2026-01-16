package org.muratcant.bookstack.features.bookcopy.test

import org.muratcant.bookstack.features.book.domain.Book
import org.muratcant.bookstack.features.book.test.BookBuilder
import org.muratcant.bookstack.features.bookcopy.domain.BookCopy
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.bookcopy.domain.UsageType
import java.time.LocalDateTime
import java.util.UUID

object BookCopyBuilder {
    fun aBookCopy(
        id: UUID = UUID.randomUUID(),
        book: Book = BookBuilder.aBook(),
        barcode: String = "BC-${UUID.randomUUID().toString().take(8).uppercase()}",
        usageType: UsageType = UsageType.BOTH,
        status: CopyStatus = CopyStatus.AVAILABLE,
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now()
    ): BookCopy = BookCopy(
        id = id,
        book = book,
        barcode = barcode,
        usageType = usageType,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun anAvailableCopy(
        id: UUID = UUID.randomUUID(),
        book: Book = BookBuilder.aBook(),
        barcode: String = "BC-${UUID.randomUUID().toString().take(8).uppercase()}"
    ) = aBookCopy(
        id = id,
        book = book,
        barcode = barcode,
        usageType = UsageType.BOTH,
        status = CopyStatus.AVAILABLE
    )

    fun aLoanedCopy(
        id: UUID = UUID.randomUUID(),
        book: Book = BookBuilder.aBook(),
        barcode: String = "BC-${UUID.randomUUID().toString().take(8).uppercase()}"
    ) = aBookCopy(
        id = id,
        book = book,
        barcode = barcode,
        status = CopyStatus.LOANED
    )

    fun aReadingRoomOnlyCopy(
        id: UUID = UUID.randomUUID(),
        book: Book = BookBuilder.aBook(),
        barcode: String = "BC-${UUID.randomUUID().toString().take(8).uppercase()}"
    ) = aBookCopy(
        id = id,
        book = book,
        barcode = barcode,
        usageType = UsageType.READING_ROOM_ONLY,
        status = CopyStatus.AVAILABLE
    )

    fun aBorrowableCopy(
        id: UUID = UUID.randomUUID(),
        book: Book = BookBuilder.aBook(),
        barcode: String = "BC-${UUID.randomUUID().toString().take(8).uppercase()}"
    ) = aBookCopy(
        id = id,
        book = book,
        barcode = barcode,
        usageType = UsageType.BORROWABLE,
        status = CopyStatus.AVAILABLE
    )

    fun anOnHoldCopy(
        id: UUID = UUID.randomUUID(),
        book: Book = BookBuilder.aBook(),
        barcode: String = "BC-${UUID.randomUUID().toString().take(8).uppercase()}"
    ) = aBookCopy(
        id = id,
        book = book,
        barcode = barcode,
        usageType = UsageType.BOTH,
        status = CopyStatus.ON_HOLD
    )
}

