package org.muratcant.bookstack.features.reservation.listbymember

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class GetReservationsByMemberResponse(
    @Schema(description = "Rezervasyon listesi")
    val reservations: List<MemberReservationItem>
)

data class MemberReservationItem(
    @Schema(description = "Rezervasyon ID'si")
    val id: UUID,

    @Schema(description = "Kitap ID'si")
    val bookId: UUID,

    @Schema(description = "Kitap başlığı")
    val bookTitle: String,

    @Schema(description = "Kitap ISBN")
    val isbn: String,

    @Schema(description = "Sıra numarası")
    val queuePosition: Int,

    @Schema(description = "Rezervasyon durumu")
    val status: String,

    @Schema(description = "Son teslim alma zamanı (varsa)")
    val expiresAt: LocalDateTime?,

    @Schema(description = "Oluşturulma zamanı")
    val createdAt: LocalDateTime
)
