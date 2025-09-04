package com.jefferson.library.service;

import com.jefferson.library.dto.BookDto;
import com.jefferson.library.dto.BookRequest;
import com.jefferson.library.exception.BookNotFoundException;
import com.jefferson.library.mapper.BookMapper;
import com.jefferson.library.model.Author;
import com.jefferson.library.model.Book;
import com.jefferson.library.repository.BookRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.domain.*;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @BeforeEach
    void initTests() {

        bookService = new BookService(bookRepository, bookMapper);

        var validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.afterPropertiesSet();

        var validationInterceptor = new MethodValidationInterceptor(validatorFactory.getValidator());

        var proxyFactory = new ProxyFactory(bookService);

        proxyFactory.addAdvice(validationInterceptor);

        bookService = (BookService) proxyFactory.getProxy();
    }

    @Test
    void getAllActiveBooks_ShouldReturnPageWithActiveBooks() {

        Pageable pageable = PageRequest.of(0, 5, Sort.by("title"));

        Author author = new Author();
        author.setId(1L);
        author.setName("Test Author");

        Book book1 = Book.buildBook(1L, "Book 1", 100, author);
        Book book2 = Book.buildBook(2L, "Book 2", 200, author);

        BookDto bookDto1 = new BookDto(1L, "Book 1", 100, LocalDateTime.now());
        BookDto bookDto2 = new BookDto(1L, "Book 2", 200, LocalDateTime.now());

        Page<Book> bookPage = new PageImpl<>(List.of(book1, book2), pageable, 2);

        when(bookRepository.findAllByDeletedFalse(pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(book1)).thenReturn(bookDto1);
        when(bookMapper.toDto(book2)).thenReturn(bookDto2);

        Page<BookDto> result = bookService.getAllActiveBooks(pageable);
        List<BookDto> content = result.getContent();

        assertTrue(content.contains(bookDto1));
        assertTrue(content.contains(bookDto2));

        verify(bookRepository, times(1)).findAllByDeletedFalse(pageable);
        verify(bookMapper, times(1)).toDto(book1);
        verify(bookMapper, times(1)).toDto(book2);

    }

    @Test
    void getAllActiveBooks_ShouldThrowConstraintViolationException_NullArg() {

        assertThatThrownBy(() -> bookService.getAllActiveBooks(null))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("pageable") &&
                                            v.getMessage().equals("Pageable arg mustn't be null"));
                });

        verifyNoInteractions(bookRepository);
        verifyNoInteractions(bookMapper);
    }

    @Test
    void getActiveBookById_ShouldReturnBook() {

        Long bookId = 2L;
        Long authorId = 1L;
        int numberOfPages = 100;
        Author author = Author.build(authorId, "Test Author");

        Book book = Book.buildBook(bookId, "Book 1", numberOfPages, author);

        BookDto bookDto = new BookDto(authorId, "Book 1", numberOfPages, LocalDateTime.now());

        BookDto bookDtoExpected = new BookDto(authorId, "Book 1", numberOfPages, LocalDateTime.now());

        when(bookRepository.findByIdAndDeletedFalse(bookId)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        BookDto result = bookService.getActiveBookById(bookId);

        assertEquals(bookDtoExpected, result);

        verify(bookRepository, times(1)).findByIdAndDeletedFalse(bookId);
        verify(bookMapper, times(1)).toDto(book);

    }

    @Test
    void getActiveBookById_ShouldThrowBookNotFoundException() {

        Long bookId = 100L;

        when(bookRepository.findByIdAndDeletedFalse(bookId)).thenReturn(Optional.empty());

        BookNotFoundException exception = assertThrows(BookNotFoundException.class,
                () -> bookService.getActiveBookById(bookId));

        assertEquals("Book not found for id: " + bookId, exception.getMessage());

        verify(bookRepository, times(1)).findByIdAndDeletedFalse(bookId);
        verifyNoInteractions(bookMapper);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    void getActiveBookById_ShouldThrowConstraintViolationException_NullArg() {

        assertThatThrownBy(() -> bookService.getActiveBookById(null))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("bookId") &&
                                            v.getMessage().equals("Book id mustn't be null"));
                });

        verifyNoInteractions(bookRepository);
        verifyNoInteractions(bookMapper);
    }

    @Test
    void getActiveBookById_ShouldThrowConstraintViolationException_NotPositiveArg() {

        assertThatThrownBy(() -> bookService.getActiveBookById(0L))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("bookId") &&
                                            v.getMessage().equals("Book id must be positive"));
                });

        verifyNoInteractions(bookRepository);
        verifyNoInteractions(bookMapper);
    }

    @Test
    void updateBookInfo_ShouldReturnUpdatedBook() {

        Long bookId = 2L;
        Long authorId = 1L;
        int numberOfPages = 100;
        Author author = Author.build(authorId, "Test Author");
        Book book = Book.buildBook(bookId, "Book 1", numberOfPages, author);

        BookDto bookDto = new BookDto(authorId, "Changed Title", 200, LocalDateTime.now());
        BookRequest bookRequest = new BookRequest(authorId, "Changed Title", 200);
        Book updatedBook = Book.buildBook(bookId, "Changed Title", 200, author);

        BookDto bookDtoExpected = new BookDto(authorId, "Changed Title", 200, LocalDateTime.now());

        when(bookRepository.findByIdAndDeletedFalse(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(updatedBook);
        when(bookMapper.toDto(updatedBook)).thenReturn(bookDto);

        BookDto result = bookService.updateBookInfo(bookId, bookRequest);

        assertEquals(bookDtoExpected, result);

        verify(bookRepository, times(1)).findByIdAndDeletedFalse(bookId);
        verify(bookRepository, times(1)).save(book);
        verify(bookMapper, times(1)).toDto(updatedBook);

    }

    @Test
    void updateBookInfo_ShouldThrowBookNotFoundException() {

        Long bookId = 100L;
        BookRequest bookRequest = new BookRequest(1L, "Changed Title", 200);

        when(bookRepository.findByIdAndDeletedFalse(bookId)).thenReturn(Optional.empty());

        BookNotFoundException exception = assertThrows(BookNotFoundException.class,
                () -> bookService.updateBookInfo(bookId, bookRequest));

        assertEquals("Book not found for id: " + bookId, exception.getMessage());

        verify(bookRepository, times(1)).findByIdAndDeletedFalse(bookId);
        verifyNoInteractions(bookMapper);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    void updateBookInfo_ShouldThrowConstraintViolationException_NullArg() {

        assertThatThrownBy(() -> bookService.updateBookInfo(null, null))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(2);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("bookId") &&
                                            v.getMessage().equals("Book id mustn't be null"))
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("bookRequest") &&
                                            v.getMessage().equals("Book request mustn't be null"));
                });

        verifyNoInteractions(bookRepository);
        verifyNoInteractions(bookMapper);
    }

    @Test
    void updateBookInfo_ShouldThrowConstraintViolationException_BadArg() {

        BookRequest bookRequest = new BookRequest(null, null, null);

        assertThatThrownBy(() -> bookService.updateBookInfo(0L, bookRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(4);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("bookId") &&
                                            v.getMessage().equals("Book id must be positive"))
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

        verifyNoInteractions(bookRepository);
        verifyNoInteractions(bookMapper);
    }

    @Test
    void updateBookInfo_ShouldThrowConstraintViolationException_BadRequestFields() {

        BookRequest bookRequest = new BookRequest(0L, "test title", 0);

        assertThatThrownBy(() -> bookService.updateBookInfo(1L, bookRequest))
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

        verifyNoInteractions(bookRepository);
        verifyNoInteractions(bookMapper);
    }

    @Test
    void deleteBook_ShouldSoftDeleteBook() {

        Long bookId = 2L;
        Long authorId = 1L;
        int numberOfPages = 100;
        Author author = Author.build(authorId, "Test Author");
        Book book = Book.buildBook(bookId, "Book 1", numberOfPages, author);

        when(bookRepository.findByIdAndDeletedFalse(bookId)).thenReturn(Optional.of(book));

        bookService.deleteBook(bookId);

        assertTrue(book.isDeleted());
        verify(bookRepository, times(1)).findByIdAndDeletedFalse(bookId);
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void deleteBook_BookNotFound() {

        Long bookId = 2L;

        when(bookRepository.findByIdAndDeletedFalse(bookId)).thenReturn(Optional.empty());

        bookService.deleteBook(bookId);

        verify(bookRepository, times(1)).findByIdAndDeletedFalse(bookId);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    void deleteBook_ShouldThrowConstraintViolationException_NullArg() {

        assertThatThrownBy(() -> bookService.deleteBook(null))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("bookId") &&
                                            v.getMessage().equals("Book id mustn't be null"));
                });

        verifyNoInteractions(bookRepository);
        verifyNoInteractions(bookMapper);
    }

    @Test
    void deleteBook_ShouldThrowConstraintViolationException_BadArg() {

        assertThatThrownBy(() -> bookService.deleteBook(0L))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("bookId") &&
                                            v.getMessage().equals("Book id must be positive"));
                });

        verifyNoInteractions(bookRepository);
        verifyNoInteractions(bookMapper);
    }
}
