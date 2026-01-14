package org.muratcant.bookstack.features.loan.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "bookstack.loan")
data class LoanProperties(
    var defaultDurationDays: Int = 14,
    var maxExtensions: Int = 2,
    var extensionDays: Int = 7
)
