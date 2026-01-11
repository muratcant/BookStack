package org.muratcant.bookstack.features.member.delete

import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteMemberHandler(
    private val memberRepository: MemberRepository
) {
    
    @Transactional
    fun handle(id: UUID) {
        val member = memberRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Member not found with id: $id") }
        
        memberRepository.delete(member)
    }
}

