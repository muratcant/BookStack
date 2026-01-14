package org.muratcant.bookstack.features.book.search

import org.muratcant.bookstack.features.book.domain.Book
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SearchBooksHandler(
    private val bookRepository: BookRepository
) {
    @Transactional(readOnly = true)
    fun handle(query: String): SearchBooksResponse {
        val books = if (query.isBlank()) {
            emptyList()
        } else {
            bookRepository.search(query)
        }

        return SearchBooksResponse(
            query = query,
            count = books.size,
            books = books.map { it.toItem() }
        )
    }

    private fun Book.toItem() = SearchBookItem(
        id = id,
        isbn = isbn,
        title = title,
        authors = authors,
        publishedYear = publishedYear
    )
}

