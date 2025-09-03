package com.jefferson.library.dto;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.Objects;

public record BookDto(
        @JsonView(AuthorDtoViews.WithBooks.class)
        @NotNull(message = "Book dto: author id mustn't be null")
        @Positive(message = "Book dto: author id must be positive")
        Long authorId,

        @JsonView(AuthorDtoViews.WithBooks.class)
        @NotBlank(message = "Book dto: Book title is null or empty")
        String title,

        @JsonView(AuthorDtoViews.WithBooks.class)
        @NotNull(message = "Book dto: number of pages mustn't be null")
        @Positive(message = "Book dto: number of pages must be positive")
        Integer pagesNumber,

        @JsonView(AuthorDtoViews.WithBooks.class)
        LocalDateTime createdAt) {

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        BookDto other = (BookDto) obj;
        return Objects.equals(authorId, other.authorId)
                && Objects.equals(title, other.title)
                && Objects.equals(pagesNumber, other.pagesNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorId, title, pagesNumber);
    }
}
