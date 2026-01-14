package org.muratcant.bookstack.features.member.activate

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class ActivateMemberResponse(
    @Schema(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: UUID,

    @Schema(description = "Unique membership number", example = "MBR-ABC12345")
    val membershipNumber: String,

    @Schema(description = "Member's first name", example = "John")
    val firstName: String,

    @Schema(description = "Member's last name", example = "Doe")
    val lastName: String,

    @Schema(description = "Member's email address", example = "john.doe@example.com")
    val email: String,

    @Schema(description = "Member status", example = "ACTIVE")
    val status: String
)

