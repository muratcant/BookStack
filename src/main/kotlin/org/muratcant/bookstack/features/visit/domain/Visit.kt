package org.muratcant.bookstack.features.visit.domain

import jakarta.persistence.*
import org.muratcant.bookstack.features.member.domain.Member
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "visits")
class Visit(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(nullable = false, updatable = false)
    val checkInTime: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true)
    var checkOutTime: LocalDateTime? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun isActive(): Boolean = checkOutTime == null

    fun checkOut() {
        checkOutTime = LocalDateTime.now()
    }
}
