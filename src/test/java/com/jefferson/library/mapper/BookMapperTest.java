package com.jefferson.library.mapper;

import com.jefferson.library.dto.BookDto;
import com.jefferson.library.dto.BookRequest;
import com.jefferson.library.model.Author;
import com.jefferson.library.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = BookMapperImpl.class)
public class BookMapperTest {

    @Autowired
    private BookMapper bookMapper;

    @Test
    void toDto_ShouldMapAllFields() {

        Long authorId = 1L;
        Long bookId = 2L;
        String title = "Onegin";
        int numOfPages = 324;
        Author author = Author.build(authorId, "Pushkin");
        Book book = Book.buildBook(bookId, title, numOfPages, author);
        author.addBook(book);

        BookDto expected = new BookDto(authorId, "Onegin", numOfPages, LocalDateTime.now());

        BookDto actual = bookMapper.toDto(book);

        assertEquals(expected, actual);
    }

    @Test
    void toEntity_ShouldNotMapAuthor() {

        Long authorId = 1L;
        String title = "Onegin";
        int numOfPages = 324;
        BookRequest bookRequest = new BookRequest(authorId, title, numOfPages);

        Book actual = bookMapper.toEntity(bookRequest);

        assertTrue(actual.getId() == null
                && actual.getAuthor() == null
                && actual.getTitle().equals(title)
                && actual.getPagesNumber() == numOfPages);
    }
}
