package org.muratcant.bookstack.features.loan.domain

import jakarta.persistence.*
import org.muratcant.bookstack.features.bookcopy.domain.BookCopy
import org.muratcant.bookstack.features.member.domain.Member
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "loans")
class Loan(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "copy_id", nullable = false)
    val bookCopy: BookCopy,

    @Column(nullable = false, updatable = false)
    val borrowedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var dueDate: LocalDateTime,

    @Column(nullable = true)
    var returnedAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: LoanStatus = LoanStatus.ACTIVE,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun isOverdue(): Boolean =
        status == LoanStatus.ACTIVE && LocalDateTime.now().isAfter(dueDate)

    fun returnCopy() {
        returnedAt = LocalDateTime.now()
        status = LoanStatus.RETURNED
    }

    fun markAsOverdue() {
        status = LoanStatus.OVERDUE
    }

    fun isActive(): Boolean = status == LoanStatus.ACTIVE

}
