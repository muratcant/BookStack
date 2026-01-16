package org.muratcant.bookstack.features.reservation.create

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class CreateReservationResponse(
    @Schema(description = "Rezervasyon ID'si")
    val id: UUID,

    @Schema(description = "Üye ID'si")
    val memberId: UUID,

    @Schema(description = "Üye adı soyadı")
    val memberName: String,

    @Schema(description = "Kitap ID'si")
    val bookId: UUID,

    @Schema(description = "Kitap başlığı")
    val bookTitle: String,

    @Schema(description = "Sıra numarası")
    val queuePosition: Int,

    @Schema(description = "Rezervasyon durumu")
    val status: String,

    @Schema(description = "Oluşturulma zamanı")
    val createdAt: LocalDateTime
)
