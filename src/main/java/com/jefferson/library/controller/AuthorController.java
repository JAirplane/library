package com.jefferson.library.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.jefferson.library.dto.AuthorDto;
import com.jefferson.library.dto.AuthorDtoViews;
import com.jefferson.library.dto.AuthorRequest;
import com.jefferson.library.dto.BookRequest;
import com.jefferson.library.service.AuthorService;
import com.jefferson.library.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/authors")
@Validated
public class AuthorController {

    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping(path = "/{id}")
    @JsonView(AuthorDtoViews.WithBooks.class)
    public AuthorDto getAuthor(@PathVariable
                                   @Positive(message = "Author id must be positive")
                                   Long id) {
        return authorService.getActiveAuthorById(id);
    }

    @PostMapping(path = "/new")
    @JsonView(AuthorDtoViews.Public.class)
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorDto newAuthor(@RequestBody
                                   @NotNull(message = "Author request mustn't be null")
                                   @Valid
                                   AuthorRequest authorRequest) {

        return authorService.createNewAuthor(authorRequest);
    }

    @PostMapping(path = "/book")
    @JsonView(AuthorDtoViews.WithBooks.class)
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorDto addBook(@RequestBody
                             @NotNull(message = "Book request mustn't be null")
                             @Valid
                             BookRequest bookRequest) {

        return authorService.addBookToAuthor(bookRequest);
    }

    @DeleteMapping(path = "/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable
                                 @Positive(message = "Author id must be positive")
                                 Long id) {

        authorService.deleteAuthorById(id);
    }
}
