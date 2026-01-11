package org.muratcant.bookstack.features.member.register

import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.domain.MemberRepository
import org.muratcant.bookstack.features.member.domain.MemberStatus
import org.muratcant.bookstack.shared.exception.DuplicateResourceException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RegisterMemberHandler(
    private val memberRepository: MemberRepository
) {
    
    @Transactional
    fun handle(request: RegisterMemberRequest): RegisterMemberResponse {
        validateEmailUniqueness(request.email)
        
        val member = Member(
            membershipNumber = generateMembershipNumber(),
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            phone = request.phone,
            status = MemberStatus.ACTIVE
        )
        
        val savedMember = memberRepository.save(member)
        
        return savedMember.toResponse()
    }
    
    private fun validateEmailUniqueness(email: String) {
        if (memberRepository.existsByEmail(email)) {
            throw DuplicateResourceException("Email already exists: $email")
        }
    }
    
    private fun generateMembershipNumber(): String =
        "MBR-${UUID.randomUUID().toString().take(8).uppercase()}"
    
    private fun Member.toResponse() = RegisterMemberResponse(
        id = id,
        membershipNumber = membershipNumber,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phone = phone,
        status = status.name
    )
}

