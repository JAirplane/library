package com.jefferson.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "authors")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @Column(nullable = false)
    @Getter
    @Setter
    private String name;

    @OneToMany(mappedBy = "author", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @SQLRestriction("deleted = false")
    private final List<Book> books = new ArrayList<>();

    @Column(nullable = false)
    @Getter
    @Setter
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Getter
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object a) {
        if(this == a) return true;
        if(a == null || getClass() != a.getClass()) return false;

        Author other = (Author) a;

        if(id == null && other.id == null) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hashCode(id) : getClass().hashCode();
    }

    public List<Book> getBooks() {
        return List.copyOf(books);
    }

    public void addBook(Book book) {
        books.add(book);
        book.setAuthor(this);
    }

    public void softDeleteAllBooks() {
        for(Book book: books) {
            book.setDeleted(true);
        }
    }

    public static Author build(Long id, String name) {
        Author author = new Author();
        author.setId(id);
        author.setName(name);
        return author;
    }
}
