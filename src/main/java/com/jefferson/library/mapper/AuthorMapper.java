package com.jefferson.library.mapper;

import com.jefferson.library.dto.AuthorDto;
import com.jefferson.library.model.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = BookMapper.class)
public interface AuthorMapper {

    @Mapping(target = "books", ignore = true)
    AuthorDto toDtoWithoutBooks(Author author);

    AuthorDto toDtoWithBooks(Author author);
}
