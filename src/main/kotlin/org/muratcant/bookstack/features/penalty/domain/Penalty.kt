package org.muratcant.bookstack.features.penalty.domain

import jakarta.persistence.*
import org.muratcant.bookstack.features.loan.domain.Loan
import org.muratcant.bookstack.features.member.domain.Member
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "penalties")
class Penalty(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false, unique = true)
    val loan: Loan,

    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,

    @Column(nullable = false)
    val daysOverdue: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PenaltyStatus = PenaltyStatus.UNPAID,

    @Column(nullable = true)
    var paidAt: LocalDateTime? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun pay() {
        status = PenaltyStatus.PAID
        paidAt = LocalDateTime.now()
    }

    fun waive() {
        status = PenaltyStatus.WAIVED
    }

    fun isPaid(): Boolean = status == PenaltyStatus.PAID
    fun isUnpaid(): Boolean = status == PenaltyStatus.UNPAID
}
