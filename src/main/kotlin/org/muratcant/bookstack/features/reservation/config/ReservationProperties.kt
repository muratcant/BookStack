package org.muratcant.bookstack.features.reservation.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "bookstack.reservation")
data class ReservationProperties(
    var pickupWindowDays: Int = 3
)
