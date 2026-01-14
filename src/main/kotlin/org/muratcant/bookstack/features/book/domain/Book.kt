package org.muratcant.bookstack.features.book.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "books")
class Book(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true, nullable = false)
    val isbn: String,

    @Column(nullable = false)
    var title: String,

    @ElementCollection
    @CollectionTable(name = "book_authors", joinColumns = [JoinColumn(name = "book_id")])
    @Column(name = "author")
    var authors: List<String> = emptyList(),

    @ElementCollection
    @CollectionTable(name = "book_categories", joinColumns = [JoinColumn(name = "book_id")])
    @Column(name = "category")
    var categories: List<String> = emptyList(),

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    var publishedYear: Int? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

