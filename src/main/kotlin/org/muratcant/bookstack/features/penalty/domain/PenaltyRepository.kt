package org.muratcant.bookstack.features.penalty.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.UUID

@Repository
interface PenaltyRepository : JpaRepository<Penalty, UUID> {
    fun findByMemberIdOrderByCreatedAtDesc(memberId: UUID): List<Penalty>
    fun findByMemberIdAndStatus(memberId: UUID, status: PenaltyStatus): List<Penalty>
    fun existsByLoanId(loanId: UUID): Boolean

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0) 
        FROM Penalty p 
        WHERE p.member.id = :memberId AND p.status = 'UNPAID'
    """)
    fun sumUnpaidAmountByMemberId(memberId: UUID): BigDecimal
}
