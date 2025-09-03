package com.jefferson.library.service;

import com.jefferson.library.dto.AuthorDto;
import com.jefferson.library.dto.AuthorRequest;
import com.jefferson.library.dto.BookRequest;
import com.jefferson.library.exception.AuthorNotFoundException;
import com.jefferson.library.mapper.AuthorMapper;
import com.jefferson.library.model.Author;
import com.jefferson.library.model.Book;
import com.jefferson.library.repository.AuthorRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Validated
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    @Autowired
    public AuthorService(AuthorRepository authorRepository, AuthorMapper authorMapper) {
        this.authorRepository = authorRepository;
        this.authorMapper = authorMapper;
    }

    public AuthorDto getActiveAuthorById(@NotNull(message = "Author id mustn't be null")
                                         @Positive(message = "Author id must be positive") Long authorId) {

        Author author = authorRepository.findByIdAndDeletedFalse(authorId)
                .orElseThrow(() -> new AuthorNotFoundException("Author not found for id: " + authorId));

        return authorMapper.toDtoWithBooks(author);
    }

    @Transactional
    public AuthorDto createNewAuthor(@NotNull(message = "Author request mustn't be null")
                                     @Valid
                                     AuthorRequest authorRequest) {
        Author author = Author.build(null, authorRequest.name());

        Author createdAuthor = authorRepository.save(author);

        return authorMapper.toDtoWithoutBooks(createdAuthor);
    }

    @Transactional
    public AuthorDto addBookToAuthor(@NotNull(message = "Book request mustn't be null")
                                     @Valid BookRequest bookRequest) {

        Author author = authorRepository.findByIdAndDeletedFalse(bookRequest.authorId())
                .orElseThrow(() -> new AuthorNotFoundException("Author not found for id: " + bookRequest.authorId()));

        Book book = Book.buildBook(null, bookRequest.title(), bookRequest.pagesNumber(), author);

        author.addBook(book);

        Author updatedAuthor = authorRepository.save(author);

        return authorMapper.toDtoWithBooks(updatedAuthor);
    }

    @Transactional
    public void deleteAuthorById(@NotNull(message = "Author id mustn't be null")
                                     @Positive(message = "Author id must be positive") Long authorId) {

        Optional<Author> authorOptional = authorRepository.findByIdAndDeletedFalse(authorId);

        if(authorOptional.isPresent()) {
            Author author = authorOptional.get();
            author.softDeleteAllBooks();

            authorRepository.save(author);
        }
    }
}
