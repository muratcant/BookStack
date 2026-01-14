package org.muratcant.bookstack.features.bookcopy.domain

import jakarta.persistence.*
import org.muratcant.bookstack.features.book.domain.Book
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "book_copies")
class BookCopy(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    val book: Book,

    @Column(unique = true, nullable = false)
    val barcode: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var usageType: UsageType = UsageType.BOTH,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: CopyStatus = CopyStatus.AVAILABLE,

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

