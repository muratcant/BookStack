package org.muratcant.bookstack.features.bookcopy.delete

import org.muratcant.bookstack.features.bookcopy.domain.BookCopyRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteBookCopyHandler(
    private val bookCopyRepository: BookCopyRepository
) {
    @Transactional
    fun handle(id: UUID) {
        if (!bookCopyRepository.existsById(id)) {
            throw ResourceNotFoundException("Book copy not found with id: $id")
        }
        bookCopyRepository.deleteById(id)
    }
}

