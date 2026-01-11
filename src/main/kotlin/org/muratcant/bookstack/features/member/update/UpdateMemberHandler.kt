package org.muratcant.bookstack.features.member.update

import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.shared.exception.DuplicateResourceException
import org.muratcant.bookstack.shared.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UpdateMemberHandler(
    private val memberRepository: MemberRepository
) {
    
    @Transactional
    fun handle(id: UUID, request: UpdateMemberRequest): UpdateMemberResponse {
        val member = memberRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Member not found with id: $id") }
        
        validateEmailUniqueness(id, request.email)
        
        member.firstName = request.firstName
        member.lastName = request.lastName
        member.email = request.email
        member.phone = request.phone
        
        val updatedMember = memberRepository.save(member)
        
        return updatedMember.toResponse()
    }
    
    private fun validateEmailUniqueness(memberId: UUID, email: String) {
        val existingMember = memberRepository.findByEmail(email)
        if (existingMember != null && existingMember.id != memberId) {
            throw DuplicateResourceException("Email already exists: $email")
        }
    }
    
    private fun Member.toResponse() = UpdateMemberResponse(
        id = id,
        membershipNumber = membershipNumber,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phone = phone,
        status = status.name
    )
}

