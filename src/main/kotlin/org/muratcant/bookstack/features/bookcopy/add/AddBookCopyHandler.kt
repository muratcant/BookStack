package org.muratcant.bookstack.features.bookcopy.add

import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.features.bookcopy.domain.BookCopy
import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.shared.exception.DuplicateResourceException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AddBookCopyHandler(
    private val bookCopyRepository: BookCopyRepository,
    private val bookRepository: BookRepository
) {
    @Transactional
    fun handle(request: AddBookCopyRequest): AddBookCopyResponse {
        val book = bookRepository.findById(request.bookId)
            .orElseThrow { ResourceNotFoundException("Book not found with id: ${request.bookId}") }

        validateBarcodeUniqueness(request.barcode)

        val bookCopy = BookCopy(
            book = book,
            barcode = request.barcode,
            usageType = request.usageType
        )

        val savedCopy = bookCopyRepository.save(bookCopy)

        return savedCopy.toResponse()
    }

    private fun validateBarcodeUniqueness(barcode: String) {
        if (bookCopyRepository.existsByBarcode(barcode)) {
            throw DuplicateResourceException("Barcode already exists: $barcode")
        }
    }

    private fun BookCopy.toResponse() = AddBookCopyResponse(
        id = id,
        bookId = book.id,
        bookTitle = book.title,
        barcode = barcode,
        usageType = usageType.name,
        status = status.name
    )
}

