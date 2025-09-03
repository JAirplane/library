package com.jefferson.library.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

public record AuthorRequest(@NotBlank(message = "Author request: name is null or empty")
                            String name) {

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        AuthorRequest other = (AuthorRequest) obj;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
