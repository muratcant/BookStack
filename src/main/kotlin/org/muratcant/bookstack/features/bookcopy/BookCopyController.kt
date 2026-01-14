package org.muratcant.bookstack.features.bookcopy

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.muratcant.bookstack.features.bookcopy.add.AddBookCopyHandler
import org.muratcant.bookstack.features.bookcopy.add.AddBookCopyRequest
import org.muratcant.bookstack.features.bookcopy.add.AddBookCopyResponse
import org.muratcant.bookstack.features.bookcopy.delete.DeleteBookCopyHandler
import org.muratcant.bookstack.features.bookcopy.get.GetBookCopyHandler
import org.muratcant.bookstack.features.bookcopy.get.GetBookCopyResponse
import org.muratcant.bookstack.features.bookcopy.list.ListBookCopiesHandler
import org.muratcant.bookstack.features.bookcopy.list.ListBookCopiesResponse
import org.muratcant.bookstack.features.bookcopy.listbybook.ListBookCopiesByBookHandler
import org.muratcant.bookstack.features.bookcopy.listbybook.ListBookCopiesByBookResponse
import org.muratcant.bookstack.features.bookcopy.update.UpdateBookCopyHandler
import org.muratcant.bookstack.features.bookcopy.update.UpdateBookCopyRequest
import org.muratcant.bookstack.features.bookcopy.update.UpdateBookCopyResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/copies")
@Tag(name = "BookCopy", description = "Physical book copy management operations")
class BookCopyController(
    private val addBookCopyHandler: AddBookCopyHandler,
    private val getBookCopyHandler: GetBookCopyHandler,
    private val listBookCopiesHandler: ListBookCopiesHandler,
    private val listBookCopiesByBookHandler: ListBookCopiesByBookHandler,
    private val updateBookCopyHandler: UpdateBookCopyHandler,
    private val deleteBookCopyHandler: DeleteBookCopyHandler
) {

    @PostMapping
    @Operation(summary = "Add new physical copy of a book")
    @ApiResponse(responseCode = "201", description = "Copy successfully added")
    @ApiResponse(responseCode = "400", description = "Validation error or duplicate barcode")
    @ApiResponse(responseCode = "404", description = "Book not found")
    fun addCopy(
        @Valid @RequestBody request: AddBookCopyRequest
    ): ResponseEntity<AddBookCopyResponse> {
        val response = addBookCopyHandler.handle(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book copy by ID")
    @ApiResponse(responseCode = "200", description = "Copy found")
    @ApiResponse(responseCode = "404", description = "Copy not found")
    fun getCopy(@PathVariable id: UUID): ResponseEntity<GetBookCopyResponse> {
        val response = getBookCopyHandler.handle(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    @Operation(summary = "List all book copies")
    @ApiResponse(responseCode = "200", description = "List of copies")
    fun listCopies(): ResponseEntity<ListBookCopiesResponse> {
        val response = listBookCopiesHandler.handle()
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update book copy")
    @ApiResponse(responseCode = "200", description = "Copy successfully updated")
    @ApiResponse(responseCode = "404", description = "Copy not found")
    fun updateCopy(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateBookCopyRequest
    ): ResponseEntity<UpdateBookCopyResponse> {
        val response = updateBookCopyHandler.handle(id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete book copy")
    @ApiResponse(responseCode = "204", description = "Copy successfully deleted")
    @ApiResponse(responseCode = "404", description = "Copy not found")
    fun deleteCopy(@PathVariable id: UUID): ResponseEntity<Void> {
        deleteBookCopyHandler.handle(id)
        return ResponseEntity.noContent().build()
    }
}

@RestController
@RequestMapping("/api/books")
@Tag(name = "BookCopy", description = "Book copies by book")
class BookCopiesByBookController(
    private val listBookCopiesByBookHandler: ListBookCopiesByBookHandler
) {
    @GetMapping("/{bookId}/copies")
    @Operation(summary = "List all copies of a specific book")
    @ApiResponse(responseCode = "200", description = "List of copies for the book")
    @ApiResponse(responseCode = "404", description = "Book not found")
    fun listCopiesByBook(@PathVariable bookId: UUID): ResponseEntity<ListBookCopiesByBookResponse> {
        val response = listBookCopiesByBookHandler.handle(bookId)
        return ResponseEntity.ok(response)
    }
}

