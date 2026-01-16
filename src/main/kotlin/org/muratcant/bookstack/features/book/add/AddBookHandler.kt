package org.muratcant.bookstack.features.book.add

import org.muratcant.bookstack.features.book.domain.Book
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.shared.exception.DuplicateResourceException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AddBookHandler(
    private val bookRepository: BookRepository
) {
    @Transactional
    fun handle(request: AddBookRequest): AddBookResponse {
        validateIsbnUniqueness(request.isbn)

        val book = Book(
            isbn = request.isbn,
            title = request.title,
            authors = request.authors,
            categories = request.categories,
            description = request.description,
            publishedYear = request.publishedYear
        )

        val savedBook = bookRepository.save(book)
        return savedBook.toResponse()
    }

    private fun validateIsbnUniqueness(isbn: String) {
        if (bookRepository.existsByIsbn(isbn)) {
            throw DuplicateResourceException("Book with ISBN already exists: $isbn")
        }
    }

    private fun Book.toResponse() = AddBookResponse(
        id = id,
        isbn = isbn,
        title = title,
        authors = authors,
        categories = categories,
        description = description,
        publishedYear = publishedYear
    )
}

