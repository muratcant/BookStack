package org.muratcant.bookstack.features.member.get

import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetMemberHandler(
    private val memberRepository: MemberRepository
) {
    
    fun handle(id: UUID): GetMemberResponse {
        val member = memberRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Member not found with id: $id") }
        
        return member.toResponse()
    }
    
    private fun Member.toResponse() = GetMemberResponse(
        id = id,
        membershipNumber = membershipNumber,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phone = phone,
        status = status.name,
        maxActiveLoans = maxActiveLoans
    )
}

