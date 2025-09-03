package com.jefferson.library.dto;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record AuthorDto(
        @JsonView(AuthorDtoViews.Public.class)
        @NotBlank(message = "Author dto: name is null or empty")
        String name,

        @JsonView(AuthorDtoViews.WithBooks.class)
        List<BookDto> books,

        @JsonView(AuthorDtoViews.Public.class)
        LocalDateTime createdAt) {

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        AuthorDto other = (AuthorDto) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(books, other.books);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, books);
    }

}
