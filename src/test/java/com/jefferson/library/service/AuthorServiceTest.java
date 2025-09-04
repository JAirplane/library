package com.jefferson.library.service;

import com.jefferson.library.dto.AuthorDto;
import com.jefferson.library.dto.AuthorRequest;
import com.jefferson.library.dto.BookDto;
import com.jefferson.library.dto.BookRequest;
import com.jefferson.library.exception.AuthorNotFoundException;
import com.jefferson.library.mapper.AuthorMapper;
import com.jefferson.library.model.Author;
import com.jefferson.library.model.Book;
import com.jefferson.library.repository.AuthorRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceTest {

    private AuthorService authorService;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private AuthorMapper authorMapper;

    @BeforeEach
    void initTests() {

        authorService = new AuthorService(authorRepository, authorMapper);

        var validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.afterPropertiesSet();

        var validationInterceptor = new MethodValidationInterceptor(validatorFactory.getValidator());

        var proxyFactory = new ProxyFactory(authorService);

        proxyFactory.addAdvice(validationInterceptor);

        authorService = (AuthorService) proxyFactory.getProxy();
    }

    @Test
    void getActiveAuthorById_ShouldReturnAuthorWithBooks() {

        Long authorId = 1L;
        String authorName = "Pushkin";
        Long bookId = 2L;
        String bookTitle = "Onegin";
        Integer numberOfPages = 324;

        Author author = Author.build(authorId, authorName);

        Book book = Book.buildBook(bookId, bookTitle, numberOfPages, author);
        author.addBook(book);

        BookDto bookDto = new BookDto(authorId, bookTitle, numberOfPages, LocalDateTime.now());
        AuthorDto authorDtoFromMapper = new AuthorDto(authorName, List.of(bookDto), LocalDateTime.now());

        AuthorDto authorDtoExpected = new AuthorDto(authorName, List.of(bookDto), LocalDateTime.now());

        when(authorRepository.findByIdAndDeletedFalse(authorId)).thenReturn(Optional.of(author));
        when(authorMapper.toDtoWithBooks(author)).thenReturn(authorDtoFromMapper);

        AuthorDto authorDtoActual = authorService.getActiveAuthorById(authorId);

        assertEquals(authorDtoExpected, authorDtoActual);
    }

    @Test
    void getActiveAuthorById_ShouldThrowAuthorNotFoundException() {

        Long authorId = 100L;

        when(authorRepository.findByIdAndDeletedFalse(authorId)).thenReturn(Optional.empty());

        AuthorNotFoundException exception = assertThrows(AuthorNotFoundException.class,
                () -> authorService.getActiveAuthorById(authorId));

        assertEquals("Author not found for id: " + authorId, exception.getMessage());

        verify(authorRepository).findByIdAndDeletedFalse(authorId);
        verify(authorMapper, never()).toDtoWithBooks(any());
        verifyNoMoreInteractions(authorRepository);
    }

    @Test
    void getActiveAuthorById_ShouldThrowConstraintViolationException_NullArg() {

        assertThatThrownBy(() -> authorService.getActiveAuthorById(null))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("authorId") &&
                                    v.getMessage().equals("Author id mustn't be null"));
                });

        verifyNoInteractions(authorRepository);
        verifyNoInteractions(authorMapper);
    }

    @Test
    void getActiveAuthorById_ShouldThrowConstraintViolationException_NotPositiveArg() {

        assertThatThrownBy(() -> authorService.getActiveAuthorById(0L))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("authorId") &&
                                            v.getMessage().equals("Author id must be positive"));
                });

        verifyNoInteractions(authorRepository);
        verifyNoInteractions(authorMapper);
    }

    @Test
    void createNewAuthor_ShouldReturnAuthorWithoutBooks() {

        Long authorId = 1L;
        String authorName = "Pushkin";

        AuthorRequest authorRequest = new AuthorRequest(authorName);

        Author savedAuthor = Author.build(authorId, authorName);

        AuthorDto authorDtoFromMapper = new AuthorDto(authorName, null, LocalDateTime.now());

        AuthorDto authorDtoExpected = new AuthorDto(authorName, null, LocalDateTime.now());

        when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);
        when(authorMapper.toDtoWithoutBooks(savedAuthor)).thenReturn(authorDtoFromMapper);

        AuthorDto authorDtoActual = authorService.createNewAuthor(authorRequest);

        assertEquals(authorDtoExpected, authorDtoActual);
    }

    @Test
    void createNewAuthor_ShouldThrowConstraintViolationException_NullArg() {

        assertThatThrownBy(() -> authorService.createNewAuthor(null))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("authorRequest") &&
                                    v.getMessage().equals("Author request mustn't be null"));
                });

        verifyNoInteractions(authorRepository);
        verifyNoInteractions(authorMapper);
    }

    @Test
    void createNewAuthor_ShouldThrowConstraintViolationException_BadDtoFields() {

        AuthorRequest authorRequest = new AuthorRequest(null);

        assertThatThrownBy(() -> authorService.createNewAuthor(authorRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("name") &&
                                    v.getMessage().equals("Author request: name is null or empty"));
                });

        verifyNoInteractions(authorRepository);
        verifyNoInteractions(authorMapper);
    }

    @Test
    void addBookToAuthor_ShouldReturnAuthorWithBooks() {

        Long authorId = 1L;

        String authorName = "Pushkin";
        Long bookId1 = 2L;
        String bookTitle1 = "Onegin";
        Integer numberOfPages1 = 324;

        String bookTitle2 = "The Captain’s Daughter";
        Integer numberOfPages2 = 223;

        Author author = Author.build(authorId, authorName);

        Book  book = Book.buildBook(bookId1, bookTitle1, numberOfPages1, author);
        author.addBook(book);

        BookRequest bookRequest = new BookRequest(authorId, bookTitle2, numberOfPages2);

        BookDto bookDto = new BookDto(authorId, bookTitle1, numberOfPages1, LocalDateTime.now());
        BookDto bookDto2 = new BookDto(authorId, bookTitle2, numberOfPages2, LocalDateTime.now());

        Book newBook = Book.buildBook(null, bookRequest.title(), bookRequest.pagesNumber(), author);

        Author updatedAuthor = Author.build(authorId, authorName);
        updatedAuthor.addBook(book);
        updatedAuthor.addBook(newBook);

        AuthorDto authorDtoFromMapper = new AuthorDto(authorName, List.of(bookDto, bookDto2), LocalDateTime.now());

        AuthorDto authorDtoExpected = new AuthorDto(authorName, List.of(bookDto, bookDto2), LocalDateTime.now());

        when(authorRepository.findByIdAndDeletedFalse(authorId)).thenReturn(Optional.of(author));
        when(authorRepository.save(author)).thenReturn(updatedAuthor);
        when(authorMapper.toDtoWithBooks(updatedAuthor)).thenReturn(authorDtoFromMapper);

        AuthorDto authorDtoActual = authorService.addBookToAuthor(bookRequest);

        assertEquals(authorDtoExpected, authorDtoActual);
    }

    @Test
    void addBookToAuthor_ShouldThrowAuthorNotFoundException() {

        Long authorId = 1L;

        String bookTitle = "Onegin";
        Integer numberOfPages = 324;
        BookRequest bookRequest = new BookRequest(authorId, bookTitle, numberOfPages);

        when(authorRepository.findByIdAndDeletedFalse(authorId)).thenReturn(Optional.empty());

        AuthorNotFoundException exception = assertThrows(AuthorNotFoundException.class,
                () -> authorService.addBookToAuthor(bookRequest));

        assertEquals("Author not found for id: " + authorId, exception.getMessage());

        verify(authorRepository).findByIdAndDeletedFalse(authorId);
        verify(authorMapper, never()).toDtoWithBooks(any());
        verifyNoMoreInteractions(authorRepository);
    }

    @Test
    void addBookToAuthor_ShouldThrowConstraintViolationException_NullArg() {

        assertThatThrownBy(() -> authorService.addBookToAuthor(null))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("bookRequest") &&
                                    v.getMessage().equals("Book request mustn't be null"));
                });

        verifyNoInteractions(authorRepository);
        verifyNoInteractions(authorMapper);
    }

    @Test
    void addBookToAuthor_ShouldThrowConstraintViolationException_NullDtoFields() {

        BookRequest bookRequest = new BookRequest(null, null, null);

        assertThatThrownBy(() -> authorService.addBookToAuthor(bookRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(3);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("authorId") &&
                                    v.getMessage().equals("Book request: author id mustn't be null"))
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("title") &&
                                    v.getMessage().equals("Book request: Book title is null or empty"))
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("pagesNumber") &&
                                            v.getMessage().equals("Book request: number of pages mustn't be null"));
                });

        verifyNoInteractions(authorRepository);
        verifyNoInteractions(authorMapper);
    }

    @Test
    void addBookToAuthor_ShouldThrowConstraintViolationException_BadDtoFields() {

        BookRequest bookRequest = new BookRequest(0L, "title", 0);

        assertThatThrownBy(() -> authorService.addBookToAuthor(bookRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(2);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("authorId") &&
                                    v.getMessage().equals("Book request: author id must be positive"))
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("pagesNumber") &&
                                    v.getMessage().equals("Book request: number of pages must be positive"));
                });

        verifyNoInteractions(authorRepository);
        verifyNoInteractions(authorMapper);
    }

    @Test
    void deleteAuthorById_ShouldSuccessfullyDelete() {

        Long authorId = 1L;

        String authorName = "Pushkin";
        Long bookId = 2L;
        String bookTitle = "Onegin";
        Integer numberOfPages = 324;

        Author author = Author.build(authorId, authorName);

        Book  book = Book.buildBook(bookId, bookTitle, numberOfPages, author);
        author.addBook(book);

        when(authorRepository.findByIdAndDeletedFalse(authorId)).thenReturn(Optional.of(author));

        authorService.deleteAuthorById(authorId);

        verify(authorRepository).findByIdAndDeletedFalse(authorId);
        verify(authorRepository).save(author);
    }

    @Test
    void deleteAuthorById_AuthorNotFoundCase() {

        Long authorId = 1L;

        when(authorRepository.findByIdAndDeletedFalse(authorId)).thenReturn(Optional.empty());

        authorService.deleteAuthorById(authorId);

        verify(authorRepository).findByIdAndDeletedFalse(authorId);
        verifyNoMoreInteractions(authorRepository);
    }

    @Test
    void deleteAuthorById_ShouldThrowConstraintViolationException_NullArg() {

        assertThatThrownBy(() -> authorService.deleteAuthorById(null))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("authorId") &&
                                    v.getMessage().equals("Author id mustn't be null"));
                });

        verifyNoInteractions(authorRepository);
        verifyNoInteractions(authorMapper);
    }

    @Test
    void deleteAuthorById_ShouldThrowConstraintViolationException_BadArg() {

        assertThatThrownBy(() -> authorService.deleteAuthorById(0L))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("authorId") &&
                                    v.getMessage().equals("Author id must be positive"));
                });

        verifyNoInteractions(authorRepository);
        verifyNoInteractions(authorMapper);
    }
}
