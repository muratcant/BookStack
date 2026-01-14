package org.muratcant.bookstack.features.book.get

import org.muratcant.bookstack.features.book.domain.Book
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetBookHandler(
    private val bookRepository: BookRepository
) {
    @Transactional(readOnly = true)
    fun handle(id: UUID): GetBookResponse {
        val book = bookRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Book not found with id: $id") }

        return book.toResponse()
    }

    private fun Book.toResponse() = GetBookResponse(
        id = id,
        isbn = isbn,
        title = title,
        authors = authors,
        categories = categories,
        description = description,
        publishedYear = publishedYear,
        createdAt = createdAt
    )
}

