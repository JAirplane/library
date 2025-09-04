package com.jefferson.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefferson.library.dto.BookDto;
import com.jefferson.library.dto.BookRequest;
import com.jefferson.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(BookController.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @Test
    void bookPage_ShouldReturnPageOfBooks() throws Exception {

        Pageable pageable = PageRequest.of(0, 10);
        BookDto bookDto1 = new BookDto(1L, "Book One", 200, LocalDateTime.now());
        BookDto bookDto2 = new BookDto(2L, "Book Two", 300, LocalDateTime.now());

        Page<BookDto> bookPage = new PageImpl<>(List.of(bookDto1, bookDto2), pageable, 2);

        when(bookService.getAllActiveBooks(any(Pageable.class))).thenReturn(bookPage);

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Book One"))
                .andExpect(jsonPath("$.content[0].authorId").value(1))
                .andExpect(jsonPath("$.content[0].pagesNumber").value(200))
                .andExpect(jsonPath("$.content[1].title").value("Book Two"))
                .andExpect(jsonPath("$.content[1].authorId").value(2))
                .andExpect(jsonPath("$.content[1].pagesNumber").value(300))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void getBook_ShouldReturnBook() throws Exception {

        Long authorId = 1L;
        Long bookId = 2L;
        BookDto bookDto = new BookDto(authorId, "Book One", 200, LocalDateTime.now());

        when(bookService.getActiveBookById(bookId)).thenReturn(bookDto);

        mockMvc.perform(get("/api/v1/books/" + bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Book One"))
                .andExpect(jsonPath("$.authorId").value(1))
                .andExpect(jsonPath("$.pagesNumber").value(200));
    }

    @Test
    void updateBook_ShouldReturnUpdatedBook() throws Exception {

        Long authorId = 1L;
        Long bookId = 2L;
        BookRequest bookRequest = new BookRequest(authorId, "Changed", 10);
        BookDto bookDto = new BookDto(authorId, "Changed", 10, LocalDateTime.now());

        when(bookService.updateBookInfo(bookId, bookRequest)).thenReturn(bookDto);

        mockMvc.perform(put("/api/v1/books/update/" + bookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Changed"))
                .andExpect(jsonPath("$.authorId").value(1))
                .andExpect(jsonPath("$.pagesNumber").value(10));
    }

    @Test
    void deleteBook_ShouldSoftDeleteBook() throws Exception {

        long bookId = 2L;

        mockMvc.perform(delete("/api/v1/books/delete/" + bookId))
                .andExpect(status().isNoContent());
    }
}
