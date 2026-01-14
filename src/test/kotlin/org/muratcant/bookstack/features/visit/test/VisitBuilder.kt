package org.muratcant.bookstack.features.visit.test

import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.features.visit.domain.Visit
import java.time.LocalDateTime
import java.util.UUID

object VisitBuilder {
    fun aVisit(
        id: UUID = UUID.randomUUID(),
        member: Member = MemberBuilder.anActiveMember(),
        checkInTime: LocalDateTime = LocalDateTime.now(),
        checkOutTime: LocalDateTime? = null,
        createdAt: LocalDateTime = LocalDateTime.now()
    ): Visit = Visit(
        id = id,
        member = member,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        createdAt = createdAt
    )

    fun anActiveVisit(
        id: UUID = UUID.randomUUID(),
        member: Member = MemberBuilder.anActiveMember(),
        checkInTime: LocalDateTime = LocalDateTime.now()
    ) = aVisit(
        id = id,
        member = member,
        checkInTime = checkInTime,
        checkOutTime = null
    )

    fun aCompletedVisit(
        id: UUID = UUID.randomUUID(),
        member: Member = MemberBuilder.anActiveMember(),
        checkInTime: LocalDateTime = LocalDateTime.now().minusHours(2),
        checkOutTime: LocalDateTime = LocalDateTime.now()
    ) = aVisit(
        id = id,
        member = member,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime
    )
}
