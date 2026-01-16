package org.muratcant.bookstack.features.penalty

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.muratcant.bookstack.features.penalty.get.GetPenaltyHandler
import org.muratcant.bookstack.features.penalty.get.GetPenaltyResponse
import org.muratcant.bookstack.features.penalty.list.ListPenaltiesHandler
import org.muratcant.bookstack.features.penalty.list.ListPenaltiesResponse
import org.muratcant.bookstack.features.penalty.listbymember.GetPenaltiesByMemberHandler
import org.muratcant.bookstack.features.penalty.listbymember.GetPenaltiesByMemberResponse
import org.muratcant.bookstack.features.penalty.pay.PayPenaltyHandler
import org.muratcant.bookstack.features.penalty.pay.PayPenaltyResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@Tag(name = "Penalty", description = "Penalty management operations")
class PenaltyController(
    private val getPenaltyHandler: GetPenaltyHandler,
    private val getPenaltiesByMemberHandler: GetPenaltiesByMemberHandler,
    private val payPenaltyHandler: PayPenaltyHandler,
    private val listPenaltiesHandler: ListPenaltiesHandler
) {

    @GetMapping("/api/penalties")
    @Operation(summary = "List all penalties")
    @ApiResponse(responseCode = "200", description = "List of penalties")
    fun listPenalties(): ResponseEntity<ListPenaltiesResponse> {
        val response = listPenaltiesHandler.handle()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/penalties/{id}")
    @Operation(summary = "Get penalty by ID")
    @ApiResponse(responseCode = "200", description = "Penalty found")
    @ApiResponse(responseCode = "404", description = "Penalty not found")
    fun getPenalty(@PathVariable id: UUID): ResponseEntity<GetPenaltyResponse> {
        val response = getPenaltyHandler.handle(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/members/{memberId}/penalties")
    @Operation(summary = "Get member's penalties")
    @ApiResponse(responseCode = "200", description = "Member penalties list")
    @ApiResponse(responseCode = "404", description = "Member not found")
    fun getPenaltiesByMember(@PathVariable memberId: UUID): ResponseEntity<GetPenaltiesByMemberResponse> {
        val response = getPenaltiesByMemberHandler.handle(memberId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/api/penalties/{id}/pay")
    @Operation(summary = "Pay a penalty")
    @ApiResponse(responseCode = "200", description = "Penalty paid successfully")
    @ApiResponse(responseCode = "400", description = "Penalty already paid or waived")
    @ApiResponse(responseCode = "404", description = "Penalty not found")
    fun payPenalty(@PathVariable id: UUID): ResponseEntity<PayPenaltyResponse> {
        val response = payPenaltyHandler.handle(id)
        return ResponseEntity.ok(response)
    }
}
