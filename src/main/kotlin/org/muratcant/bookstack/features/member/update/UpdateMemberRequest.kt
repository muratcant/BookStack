package org.muratcant.bookstack.features.member.update

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UpdateMemberRequest(
    @field:NotBlank(message = "First name is required")
    @Schema(description = "Member's first name", example = "John")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    @Schema(description = "Member's last name", example = "Doe")
    val lastName: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @Schema(description = "Member's email address", example = "john.doe@example.com")
    val email: String,

    @Schema(description = "Member's phone number", example = "+90 555 123 4567", required = false)
    val phone: String? = null
)

