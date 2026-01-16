package org.muratcant.bookstack.features.loan.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LoanRepository : JpaRepository<Loan, UUID> {
    fun findByMemberIdAndStatusOrderByBorrowedAtDesc(memberId: UUID, status: LoanStatus): List<Loan>
    fun findByMemberIdOrderByBorrowedAtDesc(memberId: UUID): List<Loan>
    fun countByMemberIdAndStatus(memberId: UUID, status: LoanStatus): Long
   }
