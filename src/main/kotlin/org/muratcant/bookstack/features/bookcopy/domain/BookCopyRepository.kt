package org.muratcant.bookstack.features.bookcopy.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BookCopyRepository : JpaRepository<BookCopy, UUID> {
    fun existsByBarcode(barcode: String): Boolean
    fun findByBarcode(barcode: String): BookCopy?
    fun findByBookId(bookId: UUID): List<BookCopy>
}

