package org.muratcant.bookstack.features.reservation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.muratcant.bookstack.features.reservation.cancel.CancelReservationHandler
import org.muratcant.bookstack.features.reservation.create.CreateReservationHandler
import org.muratcant.bookstack.features.reservation.create.CreateReservationRequest
import org.muratcant.bookstack.features.reservation.create.CreateReservationResponse
import org.muratcant.bookstack.features.reservation.get.GetReservationHandler
import org.muratcant.bookstack.features.reservation.get.GetReservationResponse
import org.muratcant.bookstack.features.reservation.list.ListReservationsHandler
import org.muratcant.bookstack.features.reservation.list.ListReservationsResponse
import org.muratcant.bookstack.features.reservation.listbymember.GetReservationsByMemberHandler
import org.muratcant.bookstack.features.reservation.listbymember.GetReservationsByMemberResponse
import org.muratcant.bookstack.features.reservation.queue.GetReservationQueueHandler
import org.muratcant.bookstack.features.reservation.queue.GetReservationQueueResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@Tag(name = "Reservation", description = "Reservation management operations")
class ReservationController(
    private val createReservationHandler: CreateReservationHandler,
    private val cancelReservationHandler: CancelReservationHandler,
    private val getReservationHandler: GetReservationHandler,
    private val getReservationsByMemberHandler: GetReservationsByMemberHandler,
    private val getReservationQueueHandler: GetReservationQueueHandler,
    private val listReservationsHandler: ListReservationsHandler
) {

    @GetMapping("/api/reservations")
    @Operation(summary = "List all reservations")
    @ApiResponse(responseCode = "200", description = "List of reservations")
    fun listReservations(): ResponseEntity<ListReservationsResponse> {
        val response = listReservationsHandler.handle()
        return ResponseEntity.ok(response)
    }

    @PostMapping("/api/reservations")
    @Operation(summary = "Create a new reservation")
    @ApiResponse(responseCode = "201", description = "Reservation created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error or duplicate reservation")
    @ApiResponse(responseCode = "404", description = "Member or book not found")
    fun createReservation(
        @Valid @RequestBody request: CreateReservationRequest
    ): ResponseEntity<CreateReservationResponse> {
        val response = createReservationHandler.handle(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/api/reservations/{id}")
    @Operation(summary = "Get reservation by ID")
    @ApiResponse(responseCode = "200", description = "Reservation found")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    fun getReservation(@PathVariable id: UUID): ResponseEntity<GetReservationResponse> {
        val response = getReservationHandler.handle(id)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/api/reservations/{id}")
    @Operation(summary = "Cancel a reservation")
    @ApiResponse(responseCode = "204", description = "Reservation cancelled successfully")
    @ApiResponse(responseCode = "400", description = "Reservation cannot be cancelled")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    fun cancelReservation(@PathVariable id: UUID): ResponseEntity<Void> {
        cancelReservationHandler.handle(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/api/members/{memberId}/reservations")
    @Operation(summary = "Get member's active reservations")
    @ApiResponse(responseCode = "200", description = "Member reservations list")
    @ApiResponse(responseCode = "404", description = "Member not found")
    fun getReservationsByMember(
        @PathVariable memberId: UUID
    ): ResponseEntity<GetReservationsByMemberResponse> {
        val response = getReservationsByMemberHandler.handle(memberId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/books/{bookId}/reservations")
    @Operation(summary = "Get reservation queue for a book")
    @ApiResponse(responseCode = "200", description = "Reservation queue")
    @ApiResponse(responseCode = "404", description = "Book not found")
    fun getReservationQueue(
        @PathVariable bookId: UUID
    ): ResponseEntity<GetReservationQueueResponse> {
        val response = getReservationQueueHandler.handle(bookId)
        return ResponseEntity.ok(response)
    }
}
