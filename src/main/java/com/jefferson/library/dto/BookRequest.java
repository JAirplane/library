package com.jefferson.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

public record BookRequest(
        @NotNull(message = "Book request: author id mustn't be null")
        @Positive(message = "Book request: author id must be positive")
        Long authorId,

        @NotBlank(message = "Book request: Book title is null or empty")
        String title,

        @NotNull(message = "Book request: number of pages mustn't be null")
        @Positive(message = "Book request: number of pages must be positive")
        Integer pagesNumber) {

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        BookRequest other = (BookRequest) obj;
        return Objects.equals(authorId, other.authorId)
                && Objects.equals(title, other.title)
                && Objects.equals(pagesNumber, other.pagesNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorId, title, pagesNumber);
    }
}
