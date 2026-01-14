package org.muratcant.bookstack.features.bookcopy.get

import org.muratcant.bookstack.features.bookcopy.domain.BookCopy
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetBookCopyHandler(
    private val bookCopyRepository: BookCopyRepository
) {
    @Transactional(readOnly = true)
    fun handle(id: UUID): GetBookCopyResponse {
        val bookCopy = bookCopyRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Book copy not found with id: $id") }

        return bookCopy.toResponse()
    }

    private fun BookCopy.toResponse() = GetBookCopyResponse(
        id = id,
        bookId = book.id,
        bookTitle = book.title,
        bookIsbn = book.isbn,
        barcode = barcode,
        usageType = usageType.name,
        status = status.name,
        createdAt = createdAt
    )
}

