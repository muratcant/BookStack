package org.muratcant.bookstack.features.member.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface MemberRepository : JpaRepository<Member, UUID> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): Member?
}

