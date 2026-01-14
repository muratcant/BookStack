package org.muratcant.bookstack.features.visit.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VisitRepository : JpaRepository<Visit, UUID> {
    fun findByMemberIdAndCheckOutTimeIsNull(memberId: UUID): Visit?
    fun existsByMemberIdAndCheckOutTimeIsNull(memberId: UUID): Boolean
    fun findByMemberIdOrderByCheckInTimeDesc(memberId: UUID): List<Visit>
}
