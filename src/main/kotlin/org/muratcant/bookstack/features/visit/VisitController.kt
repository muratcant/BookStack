package org.muratcant.bookstack.features.visit

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.muratcant.bookstack.features.visit.checkin.CheckInHandler
import org.muratcant.bookstack.features.visit.checkin.CheckInRequest
import org.muratcant.bookstack.features.visit.checkin.CheckInResponse
import org.muratcant.bookstack.features.visit.checkout.CheckOutHandler
import org.muratcant.bookstack.features.visit.checkout.CheckOutResponse
import org.muratcant.bookstack.features.visit.get.GetVisitHandler
import org.muratcant.bookstack.features.visit.get.GetVisitResponse
import org.muratcant.bookstack.features.visit.getactive.GetActiveVisitHandler
import org.muratcant.bookstack.features.visit.getactive.GetActiveVisitResponse
import org.muratcant.bookstack.features.visit.history.GetVisitHistoryHandler
import org.muratcant.bookstack.features.visit.history.VisitHistoryResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@Tag(name = "Visit", description = "Visit management operations (check-in/check-out)")
class VisitController(
    private val checkInHandler: CheckInHandler,
    private val checkOutHandler: CheckOutHandler,
    private val getVisitHandler: GetVisitHandler,
    private val getActiveVisitHandler: GetActiveVisitHandler,
    private val getVisitHistoryHandler: GetVisitHistoryHandler
) {

    @PostMapping("/api/visits/checkin")
    @Operation(summary = "Check-in member to the library")
    @ApiResponse(responseCode = "201", description = "Check-in successful")
    @ApiResponse(responseCode = "400", description = "Member not active or already checked in")
    @ApiResponse(responseCode = "404", description = "Member not found")
    fun checkIn(
        @Valid @RequestBody request: CheckInRequest
    ): ResponseEntity<CheckInResponse> {
        val response = checkInHandler.handle(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/api/visits/{id}/checkout")
    @Operation(summary = "Check-out member from the library")
    @ApiResponse(responseCode = "200", description = "Check-out successful")
    @ApiResponse(responseCode = "400", description = "Visit already checked out")
    @ApiResponse(responseCode = "404", description = "Visit not found")
    fun checkOut(@PathVariable id: UUID): ResponseEntity<CheckOutResponse> {
        val response = checkOutHandler.handle(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/visits/{id}")
    @Operation(summary = "Get visit by ID")
    @ApiResponse(responseCode = "200", description = "Visit found")
    @ApiResponse(responseCode = "404", description = "Visit not found")
    fun getVisit(@PathVariable id: UUID): ResponseEntity<GetVisitResponse> {
        val response = getVisitHandler.handle(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/members/{memberId}/visits/active")
    @Operation(summary = "Get member's active visit")
    @ApiResponse(responseCode = "200", description = "Active visit found or null if not checked in")
    @ApiResponse(responseCode = "404", description = "Member not found")
    fun getActiveVisit(@PathVariable memberId: UUID): ResponseEntity<GetActiveVisitResponse> {
        val response = getActiveVisitHandler.handle(memberId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/members/{memberId}/visits")
    @Operation(summary = "Get member's visit history")
    @ApiResponse(responseCode = "200", description = "Visit history")
    @ApiResponse(responseCode = "404", description = "Member not found")
    fun getVisitHistory(@PathVariable memberId: UUID): ResponseEntity<VisitHistoryResponse> {
        val response = getVisitHistoryHandler.handle(memberId)
        return ResponseEntity.ok(response)
    }
}
