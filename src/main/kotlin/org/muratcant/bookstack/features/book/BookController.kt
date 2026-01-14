package org.muratcant.bookstack.features.book

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.muratcant.bookstack.features.book.add.AddBookHandler
import org.muratcant.bookstack.features.book.add.AddBookRequest
import org.muratcant.bookstack.features.book.add.AddBookResponse
import org.muratcant.bookstack.features.book.delete.DeleteBookHandler
import org.muratcant.bookstack.features.book.get.GetBookHandler
import org.muratcant.bookstack.features.book.get.GetBookResponse
import org.muratcant.bookstack.features.book.list.ListBooksHandler
import org.muratcant.bookstack.features.book.list.ListBooksResponse
import org.muratcant.bookstack.features.book.search.SearchBooksHandler
import org.muratcant.bookstack.features.book.search.SearchBooksResponse
import org.muratcant.bookstack.features.book.update.UpdateBookHandler
import org.muratcant.bookstack.features.book.update.UpdateBookRequest
import org.muratcant.bookstack.features.book.update.UpdateBookResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/books")
@Tag(name = "Book", description = "Book catalog management operations")
class BookController(
    private val addBookHandler: AddBookHandler,
    private val getBookHandler: GetBookHandler,
    private val listBooksHandler: ListBooksHandler,
    private val searchBooksHandler: SearchBooksHandler,
    private val updateBookHandler: UpdateBookHandler,
    private val deleteBookHandler: DeleteBookHandler
) {

    @PostMapping
    @Operation(summary = "Add new book to catalog")
    @ApiResponse(responseCode = "201", description = "Book successfully added")
    @ApiResponse(responseCode = "400", description = "Validation error or duplicate ISBN")
    fun addBook(
        @Valid @RequestBody request: AddBookRequest
    ): ResponseEntity<AddBookResponse> {
        val response = addBookHandler.handle(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID")
    @ApiResponse(responseCode = "200", description = "Book found")
    @ApiResponse(responseCode = "404", description = "Book not found")
    fun getBook(@PathVariable id: UUID): ResponseEntity<GetBookResponse> {
        val response = getBookHandler.handle(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    @Operation(summary = "List all books")
    @ApiResponse(responseCode = "200", description = "List of books")
    fun listBooks(): ResponseEntity<ListBooksResponse> {
        val response = listBooksHandler.handle()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/search")
    @Operation(summary = "Search books by title or ISBN")
    @ApiResponse(responseCode = "200", description = "Search results")
    fun searchBooks(
        @RequestParam q: String
    ): ResponseEntity<SearchBooksResponse> {
        val response = searchBooksHandler.handle(q)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update book")
    @ApiResponse(responseCode = "200", description = "Book successfully updated")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Book not found")
    fun updateBook(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateBookRequest
    ): ResponseEntity<UpdateBookResponse> {
        val response = updateBookHandler.handle(id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete book")
    @ApiResponse(responseCode = "204", description = "Book successfully deleted")
    @ApiResponse(responseCode = "404", description = "Book not found")
    fun deleteBook(@PathVariable id: UUID): ResponseEntity<Void> {
        deleteBookHandler.handle(id)
        return ResponseEntity.noContent().build()
    }
}

