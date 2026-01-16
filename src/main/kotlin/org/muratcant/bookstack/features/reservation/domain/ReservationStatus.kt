package org.muratcant.bookstack.features.reservation.domain

enum class ReservationStatus {
    WAITING,
    READY_FOR_PICKUP,
    FULFILLED,
    EXPIRED,
    CANCELLED
}
