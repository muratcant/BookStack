package org.muratcant.bookstack.features.member.activate

import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.domain.MemberStatus
import org.muratcant.bookstack.shared.exception.InvalidStatusTransitionException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ActivateMemberHandler(
    private val memberRepository: MemberRepository
) {
    @Transactional
    fun handle(id: UUID): ActivateMemberResponse {
        val member = memberRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Member not found with id: $id") }

        validateStatusTransition(member)

        member.status = MemberStatus.ACTIVE

        val savedMember = memberRepository.save(member)

        return savedMember.toResponse()
    }

    private fun validateStatusTransition(member: Member) {
        when (member.status) {
            MemberStatus.ACTIVE -> throw InvalidStatusTransitionException(
                "Member is already active"
            )
            MemberStatus.SUSPENDED, MemberStatus.EXPIRED -> { /* Valid transitions */ }
        }
    }

    private fun Member.toResponse() = ActivateMemberResponse(
        id = id,
        membershipNumber = membershipNumber,
        firstName = firstName,
        lastName = lastName,
        email = email,
        status = status.name
    )
}

