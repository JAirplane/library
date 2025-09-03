package com.jefferson.library.mapper;

import com.jefferson.library.dto.AuthorDto;
import com.jefferson.library.dto.BookDto;
import com.jefferson.library.model.Author;
import com.jefferson.library.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {AuthorMapperImpl.class, BookMapperImpl.class})
public class AuthorMapperTest {

    @Autowired
    private AuthorMapper authorMapper;

    @Test
    void toDtoWithBooks_ShouldMapAllFields() {

        Long authorId = 1L;
        Long bookId = 2L;
        String title = "Onegin";
        String authorName = "Pushkin";
        int numOfPages = 324;
        Author author = Author.build(authorId, authorName);
        Book book = Book.buildBook(bookId, title, numOfPages, author);
        author.addBook(book);
        BookDto bookDto = new BookDto(authorId, title, numOfPages, LocalDateTime.now());

        AuthorDto expectedAuthorDto = new AuthorDto(authorName, List.of(bookDto), LocalDateTime.now());

        AuthorDto actual = authorMapper.toDtoWithBooks(author);

        assertEquals(expectedAuthorDto, actual);
    }

    @Test
    public void toDtoWithoutBooks_ShouldIgnoreBooks() {

        Long authorId = 1L;
        Long bookId = 2L;
        String title = "Onegin";
        String authorName = "Pushkin";
        int numOfPages = 324;
        Author author = Author.build(authorId, authorName);
        Book book = Book.buildBook(bookId, title, numOfPages, author);
        author.addBook(book);

        AuthorDto expectedAuthorDto = new AuthorDto(authorName, null, LocalDateTime.now());

        AuthorDto actual = authorMapper.toDtoWithoutBooks(author);

        assertEquals(expectedAuthorDto, actual);
    }
}
