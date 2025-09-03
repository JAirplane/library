package com.jefferson.library.service;

import com.jefferson.library.dto.BookDto;
import com.jefferson.library.dto.BookRequest;
import com.jefferson.library.exception.BookNotFoundException;
import com.jefferson.library.mapper.BookMapper;
import com.jefferson.library.model.Book;
import com.jefferson.library.repository.BookRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Validated
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Autowired
    public BookService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    public Page<BookDto> getAllActiveBooks(@NotNull(message = "Pageable arg mustn't be null")
                                           Pageable pageable) {
        return bookRepository.findAllByDeletedFalse(pageable)
                .map(bookMapper::toDto);
    }

    public BookDto getActiveBookById(@NotNull(message = "Book id mustn't be null")
                               @Positive(message = "Book id must be positive") Long bookId) {

        Book book = bookRepository.findByIdAndDeletedFalse(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found for id: " + bookId));

        return bookMapper.toDto(book);
    }

    @Transactional
    public BookDto updateBookInfo(@NotNull(message = "Book id mustn't be null")
                                  @Positive(message = "Book id must be positive") Long bookId,
                                  @NotNull(message = "Book request mustn't be null")
                                  @Valid BookRequest bookRequest) {

        Book book = bookRepository.findByIdAndDeletedFalse(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found for id: " + bookId));

        book.setTitle(bookRequest.title());
        book.setPagesNumber(bookRequest.pagesNumber());

        Book updatedBook = bookRepository.save(book);

        return bookMapper.toDto(updatedBook);
    }

    @Transactional
    public void deleteBook(@NotNull(message = "Book id mustn't be null")
                               @Positive(message = "Book id must be positive") Long bookId) {

        Optional<Book> bookOptional = bookRepository.findByIdAndDeletedFalse(bookId);

        if(bookOptional.isPresent()) {
            Book book = bookOptional.get();
            book.setDeleted(true);

            bookRepository.save(book);
        }
    }
}
