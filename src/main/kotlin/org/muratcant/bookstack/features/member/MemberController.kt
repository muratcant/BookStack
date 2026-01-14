package org.muratcant.bookstack.features.member

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.muratcant.bookstack.features.member.activate.ActivateMemberHandler
import org.muratcant.bookstack.features.member.activate.ActivateMemberResponse
import org.muratcant.bookstack.features.member.delete.DeleteMemberHandler
import org.muratcant.bookstack.features.member.get.GetMemberHandler
import org.muratcant.bookstack.features.member.get.GetMemberResponse
import org.muratcant.bookstack.features.member.list.ListMembersHandler
import org.muratcant.bookstack.features.member.list.ListMembersResponse
import org.muratcant.bookstack.features.member.register.RegisterMemberHandler
import org.muratcant.bookstack.features.member.register.RegisterMemberRequest
import org.muratcant.bookstack.features.member.register.RegisterMemberResponse
import org.muratcant.bookstack.features.member.suspend.SuspendMemberHandler
import org.muratcant.bookstack.features.member.suspend.SuspendMemberResponse
import org.muratcant.bookstack.features.member.update.UpdateMemberHandler
import org.muratcant.bookstack.features.member.update.UpdateMemberRequest
import org.muratcant.bookstack.features.member.update.UpdateMemberResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/members")
@Tag(name = "Member", description = "Member management operations")
class MemberController(
    private val registerMemberHandler: RegisterMemberHandler,
    private val getMemberHandler: GetMemberHandler,
    private val listMembersHandler: ListMembersHandler,
    private val updateMemberHandler: UpdateMemberHandler,
    private val deleteMemberHandler: DeleteMemberHandler,
    private val suspendMemberHandler: SuspendMemberHandler,
    private val activateMemberHandler: ActivateMemberHandler
) {
    
    @PostMapping
    @Operation(summary = "Register new member")
    @ApiResponse(responseCode = "201", description = "Member successfully created")
    @ApiResponse(responseCode = "400", description = "Validation error or duplicate email")
    fun register(
        @Valid @RequestBody request: RegisterMemberRequest
    ): ResponseEntity<RegisterMemberResponse> {
        val response = registerMemberHandler.handle(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get member by ID")
    @ApiResponse(responseCode = "200", description = "Member found")
    @ApiResponse(responseCode = "404", description = "Member not found")
    fun getMember(@PathVariable id: UUID): ResponseEntity<GetMemberResponse> {
        val response = getMemberHandler.handle(id)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping
    @Operation(summary = "List all members")
    @ApiResponse(responseCode = "200", description = "List of members")
    fun listMembers(): ResponseEntity<ListMembersResponse> {
        val response = listMembersHandler.handle()
        return ResponseEntity.ok(response)
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update member")
    @ApiResponse(responseCode = "200", description = "Member successfully updated")
    @ApiResponse(responseCode = "400", description = "Validation error or duplicate email")
    @ApiResponse(responseCode = "404", description = "Member not found")
    fun updateMember(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateMemberRequest
    ): ResponseEntity<UpdateMemberResponse> {
        val response = updateMemberHandler.handle(id, request)
        return ResponseEntity.ok(response)
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete member")
    @ApiResponse(responseCode = "204", description = "Member successfully deleted")
    @ApiResponse(responseCode = "404", description = "Member not found")
    fun deleteMember(@PathVariable id: UUID): ResponseEntity<Void> {
        deleteMemberHandler.handle(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Suspend member")
    @ApiResponse(responseCode = "200", description = "Member successfully suspended")
    @ApiResponse(responseCode = "400", description = "Invalid status transition")
    @ApiResponse(responseCode = "404", description = "Member not found")
    fun suspendMember(@PathVariable id: UUID): ResponseEntity<SuspendMemberResponse> {
        val response = suspendMemberHandler.handle(id)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate member")
    @ApiResponse(responseCode = "200", description = "Member successfully activated")
    @ApiResponse(responseCode = "400", description = "Invalid status transition")
    @ApiResponse(responseCode = "404", description = "Member not found")
    fun activateMember(@PathVariable id: UUID): ResponseEntity<ActivateMemberResponse> {
        val response = activateMemberHandler.handle(id)
        return ResponseEntity.ok(response)
    }
}

