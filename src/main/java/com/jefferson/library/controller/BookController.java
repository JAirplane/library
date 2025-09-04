package com.jefferson.library.controller;

import com.jefferson.library.dto.BookDto;
import com.jefferson.library.dto.BookRequest;
import com.jefferson.library.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/books")
@Validated
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Page<BookDto> bookPage(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        return bookService.getAllActiveBooks(pageable);
    }

    @GetMapping(path = "/{id}")
    public BookDto getBook(@PathVariable
                               @Positive(message = "Book id must be positive")
                               Long id) {
        return bookService.getActiveBookById(id);
    }

    @PutMapping(path = "/update/{id}")
    public BookDto updateBook(@PathVariable
                                  @Positive(message = "Book id must be positive")
                                  Long id,
                              @RequestBody
                                  @NotNull(message = "Book request mustn't be null")
                                  @Valid
                              BookRequest bookRequest) {
        return bookService.updateBookInfo(id, bookRequest);
    }

    @DeleteMapping(path = "/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable
                               @Positive(message = "Book id must be positive")
                               Long id) {
        bookService.deleteBook(id);
    }
}
