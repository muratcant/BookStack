package org.muratcant.bookstack.shared.exception

import java.time.LocalDateTime

data class ErrorResponse(
    val error: String,
    val details: List<String>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

