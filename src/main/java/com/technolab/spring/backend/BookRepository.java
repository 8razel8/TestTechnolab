package com.technolab.spring.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("bookRepository")
public interface BookRepository extends JpaRepository<Book, Long> {
    Book findByName(String name);
    Book findByIsbn(String isbn);
}