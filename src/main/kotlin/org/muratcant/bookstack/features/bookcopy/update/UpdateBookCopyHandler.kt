package org.muratcant.bookstack.features.bookcopy.update

import org.muratcant.bookstack.features.bookcopy.domain.BookCopy
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UpdateBookCopyHandler(
    private val bookCopyRepository: BookCopyRepository
) {
    @Transactional
    fun handle(id: UUID, request: UpdateBookCopyRequest): UpdateBookCopyResponse {
        val bookCopy = bookCopyRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Book copy not found with id: $id") }

        bookCopy.usageType = request.usageType
        bookCopy.status = request.status

        val savedCopy = bookCopyRepository.save(bookCopy)

        return savedCopy.toResponse()
    }

    private fun BookCopy.toResponse() = UpdateBookCopyResponse(
        id = id,
        bookId = book.id,
        bookTitle = book.title,
        barcode = barcode,
        usageType = usageType.name,
        status = status.name
    )
}

