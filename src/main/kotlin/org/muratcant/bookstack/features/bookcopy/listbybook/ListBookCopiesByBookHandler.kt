package org.muratcant.bookstack.features.bookcopy.listbybook

import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.bookcopy.domain.BookCopy
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.features.bookcopy.list.BookCopyItem
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListBookCopiesByBookHandler(
    private val bookCopyRepository: BookCopyRepository,
    private val bookRepository: BookRepository
) {
    @Transactional(readOnly = true)
    fun handle(bookId: UUID): ListBookCopiesByBookResponse {
        val book = bookRepository.findById(bookId)
            .orElseThrow { ResourceNotFoundException("Book not found with id: $bookId") }

        val copies = bookCopyRepository.findByBookId(bookId)

        return ListBookCopiesByBookResponse(
            bookId = book.id,
            bookTitle = book.title,
            copies = copies.map { it.toItem() }
        )
    }

    private fun BookCopy.toItem() = BookCopyItem(
        id = id,
        bookId = book.id,
        bookTitle = book.title,
        barcode = barcode,
        usageType = usageType.name,
        status = status.name
    )
}

