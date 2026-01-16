package org.muratcant.bookstack.features.member.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "members")
class Member(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true, nullable = false, updatable = false)
    val membershipNumber: String,

    @Column(nullable = false)
    var firstName: String,

    @Column(nullable = false)
    var lastName: String,

    @Column(unique = true, nullable = false)
    var email: String,

    @Column(nullable = true)
    var phone: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: MemberStatus = MemberStatus.ACTIVE,

    @Column(nullable = false)
    var maxActiveLoans: Int = 5,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    fun isActive() = status == MemberStatus.ACTIVE

    fun update(firstName: String, lastName: String, email: String, phone: String?) {
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.phone = phone
    }
}

