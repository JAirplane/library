package com.jefferson.library.repository;

import com.jefferson.library.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface BookRepository extends CrudRepository<Book, Long>, PagingAndSortingRepository<Book, Long> {

    Page<Book> findAllByDeletedFalse(Pageable pageable);
    Optional<Book> findByIdAndDeletedFalse(Long id);
}
