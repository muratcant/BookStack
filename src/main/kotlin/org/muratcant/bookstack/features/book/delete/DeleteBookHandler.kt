package org.muratcant.bookstack.features.book.delete

import org.muratcant.bookstack.features.book.domain.BookRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteBookHandler(
    private val bookRepository: BookRepository
) {
    @Transactional
    fun handle(id: UUID) {
        if (!bookRepository.existsById(id)) {
            throw ResourceNotFoundException("Book not found with id: $id")
        }
        bookRepository.deleteById(id)
    }
}

