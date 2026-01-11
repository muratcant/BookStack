package org.muratcant.bookstack.shared.domain

import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PreUpdate
import java.time.LocalDateTime
import java.util.UUID

@MappedSuperclass
abstract class BaseEntity(
    @Id
    open val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, updatable = false)
    open val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    open var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

