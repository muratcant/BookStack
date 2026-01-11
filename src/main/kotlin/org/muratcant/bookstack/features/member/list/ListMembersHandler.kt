package org.muratcant.bookstack.features.member.list

import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.springframework.stereotype.Service

@Service
class ListMembersHandler(
    private val memberRepository: MemberRepository
) {
    
    fun handle(): ListMembersResponse {
        val members = memberRepository.findAll()
        
        return ListMembersResponse(
            members = members.map { it.toItem() }
        )
    }
    
    private fun Member.toItem() = MemberItem(
        id = id.toString(),
        membershipNumber = membershipNumber,
        fullName = "$firstName $lastName",
        email = email,
        status = status.name
    )
}

