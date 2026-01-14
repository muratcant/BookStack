package org.muratcant.bookstack.features.book.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BookRepository : JpaRepository<Book, UUID> {
    fun existsByIsbn(isbn: String): Boolean
    fun findByIsbn(isbn: String): Book?

    @Query("""
        SELECT b FROM Book b 
        WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    fun search(query: String): List<Book>
}

