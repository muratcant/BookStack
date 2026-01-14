package org.muratcant.bookstack.features.bookcopy.update

import io.swagger.v3.oas.annotations.media.Schema
import org.muratcant.bookstack.features.bookcopy.domain.CopyStatus
import org.muratcant.bookstack.features.bookcopy.domain.UsageType

data class UpdateBookCopyRequest(
    @Schema(description = "Usage type of the copy", example = "BORROWABLE")
    val usageType: UsageType,

    @Schema(description = "Current status of the copy", example = "AVAILABLE")
    val status: CopyStatus
)

