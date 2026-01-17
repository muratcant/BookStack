package org.muratcant.bookstack.features.visit.checkin

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class CheckInRequest(
    @field:NotNull(message = "Member ID is required")
    @Schema(description = "ID of the member checking in", example = "123e4567-e89b-12d3-a456-426614174000")
    val memberId: UUID
)
