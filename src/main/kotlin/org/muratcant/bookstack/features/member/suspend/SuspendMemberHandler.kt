package org.muratcant.bookstack.features.member.suspend

import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.domain.MemberStatus
import org.muratcant.bookstack.shared.exception.InvalidStatusTransitionException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class SuspendMemberHandler(
    private val memberRepository: MemberRepository
) {
    @Transactional
    fun handle(id: UUID): SuspendMemberResponse {
        val member = memberRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Member not found with id: $id") }

        validateStatusTransition(member)

        member.status = MemberStatus.SUSPENDED

        return memberRepository.save(member).toResponse()
    }

    private fun validateStatusTransition(member: Member) {
        when (member.status) {
            MemberStatus.SUSPENDED -> throw InvalidStatusTransitionException(
                "Member is already suspended"
            )
            MemberStatus.EXPIRED -> throw InvalidStatusTransitionException(
                "Cannot suspend an expired member. Please activate first."
            )
            MemberStatus.ACTIVE -> { /* Valid transition */ }
        }
    }

    private fun Member.toResponse() = SuspendMemberResponse(
        id = id,
        membershipNumber = membershipNumber,
        firstName = firstName,
        lastName = lastName,
        email = email,
        status = status.name
    )
}

