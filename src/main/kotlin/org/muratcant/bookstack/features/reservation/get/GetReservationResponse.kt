package org.muratcant.bookstack.features.reservation.get

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class GetReservationResponse(
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

    @Schema(description = "Atanan kopya ID'si (varsa)")
    val copyId: UUID?,

    @Schema(description = "Atanan kopya barkodu (varsa)")
    val barcode: String?,

    @Schema(description = "Sıra numarası")
    val queuePosition: Int,

    @Schema(description = "Rezervasyon durumu")
    val status: String,

    @Schema(description = "Bildirim zamanı")
    val notifiedAt: LocalDateTime?,

    @Schema(description = "Son teslim alma zamanı")
    val expiresAt: LocalDateTime?,

    @Schema(description = "Oluşturulma zamanı")
    val createdAt: LocalDateTime
)
