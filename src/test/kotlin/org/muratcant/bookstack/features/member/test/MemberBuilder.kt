package org.muratcant.bookstack.features.member.test

import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.domain.MemberStatus
import java.time.LocalDateTime
import java.util.UUID

object MemberBuilder {
    fun aMember(
        id: UUID = UUID.randomUUID(),
        membershipNumber: String = "MBR-${UUID.randomUUID().toString().take(8).uppercase()}",
        firstName: String = "John",
        lastName: String = "Doe",
        email: String = "john.doe@example.com",
        phone: String? = "+90 555 123 4567",
        status: MemberStatus = MemberStatus.ACTIVE,
        maxActiveLoans: Int = 5,
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now()
    ): Member = Member(
        id = id,
        membershipNumber = membershipNumber,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phone = phone,
        status = status,
        maxActiveLoans = maxActiveLoans,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    fun anActiveMember(
        id: UUID = UUID.randomUUID(),
        firstName: String = "John",
        lastName: String = "Doe",
        email: String = "john.doe@example.com",
        phone: String? = "+90 555 123 4567"
    ) = aMember(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phone = phone,
        status = MemberStatus.ACTIVE
    )
    
    fun aSuspendedMember(
        id: UUID = UUID.randomUUID(),
        firstName: String = "John",
        lastName: String = "Doe",
        email: String = "john.doe@example.com",
        phone: String? = "+90 555 123 4567"
    ) = aMember(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phone = phone,
        status = MemberStatus.SUSPENDED
    )
    
    fun anExpiredMember(
        id: UUID = UUID.randomUUID(),
        firstName: String = "John",
        lastName: String = "Doe",
        email: String = "john.doe@example.com",
        phone: String? = "+90 555 123 4567"
    ) = aMember(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phone = phone,
        status = MemberStatus.EXPIRED
    )
}

