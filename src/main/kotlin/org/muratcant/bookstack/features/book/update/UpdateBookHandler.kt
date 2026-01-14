package org.muratcant.bookstack.features.book.update

import org.muratcant.bookstack.features.book.domain.Book
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UpdateBookHandler(
    private val bookRepository: BookRepository
) {
    @Transactional
    fun handle(id: UUID, request: UpdateBookRequest): UpdateBookResponse {
        val book = bookRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Book not found with id: $id") }

        book.title = request.title
        book.authors = request.authors
        book.categories = request.categories
        book.description = request.description
        book.publishedYear = request.publishedYear

        val savedBook = bookRepository.save(book)

        return savedBook.toResponse()
    }

    private fun Book.toResponse() = UpdateBookResponse(
        id = id,
        isbn = isbn,
        title = title,
        authors = authors,
        categories = categories,
        description = description,
        publishedYear = publishedYear
    )
}

