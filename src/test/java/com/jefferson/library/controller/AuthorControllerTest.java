package com.jefferson.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefferson.library.dto.AuthorDto;
import com.jefferson.library.dto.AuthorRequest;
import com.jefferson.library.dto.BookDto;
import com.jefferson.library.dto.BookRequest;
import com.jefferson.library.service.AuthorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest(AuthorController.class)
public class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthorService authorService;

    @Test
    void getAuthor_ShouldReturnAuthorWithBooks() throws Exception {

        Long authorId = 1L;
        String bookTitle = "Book One";
        int numberOfPages = 200;
        BookDto bookDto = new BookDto(authorId, bookTitle, numberOfPages, LocalDateTime.now());

        AuthorDto authorDto = new AuthorDto("Test name", List.of(bookDto), LocalDateTime.now());

        when(authorService.getActiveAuthorById(authorId)).thenReturn(authorDto);

        mockMvc.perform(get("/api/v1/authors/" + authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test name"))
                .andExpect(jsonPath("$.books[0].authorId").value(authorId))
                .andExpect(jsonPath("$.books[0].title").value(bookTitle))
                .andExpect(jsonPath("$.books[0].pagesNumber").value(numberOfPages));
    }

    @Test
    void newAuthor_ShouldReturnAuthorWithoutBooks() throws Exception {

        AuthorRequest authorRequest = new AuthorRequest("Test name");
        AuthorDto authorDto = new AuthorDto("Test name", null, LocalDateTime.now());

        when(authorService.createNewAuthor(authorRequest)).thenReturn(authorDto);

        mockMvc.perform(post("/api/v1/authors/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test name"))
                .andExpect(jsonPath("$.books").doesNotExist());
    }

    @Test
    void addBook_ShouldReturnAuthorWithBooks() throws Exception {

        Long authorId = 1L;
        String bookTitle = "Book One";
        int numberOfPages = 200;
        BookDto bookDto = new BookDto(authorId, bookTitle, numberOfPages, LocalDateTime.now());
        BookRequest bookRequest = new BookRequest(authorId, bookTitle, numberOfPages);
        AuthorDto authorDto = new AuthorDto("Test name", List.of(bookDto), LocalDateTime.now());

        when(authorService.addBookToAuthor(bookRequest)).thenReturn(authorDto);

        mockMvc.perform(post("/api/v1/authors/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test name"))
                .andExpect(jsonPath("$.books[0].authorId").value(authorId))
                .andExpect(jsonPath("$.books[0].title").value(bookTitle))
                .andExpect(jsonPath("$.books[0].pagesNumber").value(numberOfPages));
    }

    @Test
    void deleteAuthor_ShouldSoftDeleteAuthorAndHisBooks() throws Exception {

        mockMvc.perform(delete("/api/v1/authors/delete/" + 1L))
                .andExpect(status().isNoContent());
    }
}
