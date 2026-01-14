package org.muratcant.bookstack.features.bookcopy.list

import org.muratcant.bookstack.features.bookcopy.domain.BookCopy
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListBookCopiesHandler(
    private val bookCopyRepository: BookCopyRepository
) {
    @Transactional(readOnly = true)
    fun handle(): ListBookCopiesResponse {
        val copies = bookCopyRepository.findAll()
        return ListBookCopiesResponse(
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

