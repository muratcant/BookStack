package org.muratcant.bookstack.features.penalty.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConfigurationProperties(prefix = "bookstack.penalty")
data class PenaltyProperties(
    var dailyFee: BigDecimal = BigDecimal("1.00"),
    var blockingThreshold: BigDecimal = BigDecimal("10.00")
)
