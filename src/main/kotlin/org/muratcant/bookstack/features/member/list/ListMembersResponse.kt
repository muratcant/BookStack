package org.muratcant.bookstack.features.member.list

import io.swagger.v3.oas.annotations.media.Schema

data class ListMembersResponse(
    @Schema(description = "List of members")
    val members: List<MemberItem>
)

data class MemberItem(
    @Schema(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: String,

    @Schema(description = "Unique membership number", example = "MBR-ABC12345")
    val membershipNumber: String,

    @Schema(description = "Member's full name", example = "John Doe")
    val fullName: String,

    @Schema(description = "Member's email address", example = "john.doe@example.com")
    val email: String,

    @Schema(description = "Member status", example = "ACTIVE")
    val status: String
)

