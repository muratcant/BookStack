package org.muratcant.bookstack.features.book.list

import org.muratcant.bookstack.features.book.domain.Book
import org.muratcant.bookstack.features.book.domain.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListBooksHandler(
    private val bookRepository: BookRepository
) {
    @Transactional(readOnly = true)
    fun handle(): ListBooksResponse {
        val books = bookRepository.findAll()
        return ListBooksResponse(
            books = books.map { it.toItem() }
        )
    }

    private fun Book.toItem() = BookItem(
        id = id,
        isbn = isbn,
        title = title,
        authors = authors,
        categories = categories,
        publishedYear = publishedYear
    )
}

