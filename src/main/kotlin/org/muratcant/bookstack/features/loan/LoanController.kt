package org.muratcant.bookstack.features.loan

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.muratcant.bookstack.features.loan.borrow.BorrowCopyHandler
import org.muratcant.bookstack.features.loan.borrow.BorrowCopyRequest
import org.muratcant.bookstack.features.loan.borrow.BorrowCopyResponse
import org.muratcant.bookstack.features.loan.get.GetLoanHandler
import org.muratcant.bookstack.features.loan.get.GetLoanResponse
import org.muratcant.bookstack.features.loan.history.GetLoanHistoryHandler
import org.muratcant.bookstack.features.loan.history.GetLoanHistoryResponse
import org.muratcant.bookstack.features.loan.listactive.GetActiveLoansHandler
import org.muratcant.bookstack.features.loan.listactive.GetActiveLoansResponse
import org.muratcant.bookstack.features.loan.returnloan.ReturnCopyHandler
import org.muratcant.bookstack.features.loan.returnloan.ReturnCopyResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@Tag(name = "Loan", description = "Loan management operations (borrow/return)")
class LoanController(
    private val borrowCopyHandler: BorrowCopyHandler,
    private val returnCopyHandler: ReturnCopyHandler,
    private val getLoanHandler: GetLoanHandler,
    private val getActiveLoansHandler: GetActiveLoansHandler,
    private val getLoanHistoryHandler: GetLoanHistoryHandler
) {

    @PostMapping("/api/loans")
    @Operation(summary = "Borrow a book copy")
    @ApiResponse(responseCode = "201", description = "Copy successfully borrowed")
    @ApiResponse(responseCode = "400", description = "Member not checked in, copy not available, or max loans exceeded")
    @ApiResponse(responseCode = "404", description = "Member or copy not found")
    fun borrowCopy(
        @Valid @RequestBody request: BorrowCopyRequest
    ): ResponseEntity<BorrowCopyResponse> {
        val response = borrowCopyHandler.handle(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/api/loans/{id}/return")
    @Operation(summary = "Return a borrowed copy")
    @ApiResponse(responseCode = "200", description = "Copy successfully returned")
    @ApiResponse(responseCode = "400", description = "Loan is not active")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    fun returnCopy(@PathVariable id: UUID): ResponseEntity<ReturnCopyResponse> {
        val response = returnCopyHandler.handle(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/loans/{id}")
    @Operation(summary = "Get loan by ID")
    @ApiResponse(responseCode = "200", description = "Loan found")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    fun getLoan(@PathVariable id: UUID): ResponseEntity<GetLoanResponse> {
        val response = getLoanHandler.handle(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/members/{memberId}/loans/active")
    @Operation(summary = "Get member's active loans")
    @ApiResponse(responseCode = "200", description = "Active loans list")
    @ApiResponse(responseCode = "404", description = "Member not found")
    fun getActiveLoans(@PathVariable memberId: UUID): ResponseEntity<GetActiveLoansResponse> {
        val response = getActiveLoansHandler.handle(memberId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/members/{memberId}/loans")
    @Operation(summary = "Get member's loan history")
    @ApiResponse(responseCode = "200", description = "Loan history")
    @ApiResponse(responseCode = "404", description = "Member not found")
    fun getLoanHistory(@PathVariable memberId: UUID): ResponseEntity<GetLoanHistoryResponse> {
        val response = getLoanHistoryHandler.handle(memberId)
        return ResponseEntity.ok(response)
    }
}
