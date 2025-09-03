package com.jefferson.library.repository;

import com.jefferson.library.model.Author;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AuthorRepository extends CrudRepository<Author, Long> {

    Optional<Author> findByIdAndDeletedFalse(Long id);
}
