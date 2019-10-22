package com.technolab.spring.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("bookService")
public class BookService {

    private BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book findBookByName(String name) {
        return bookRepository.findByName(name);
    }

    public void saveBook(Book book) {
        bookRepository.save(book);
    }

    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    public void deleteBook(Book book) {
        bookRepository.delete(book);
        Broadcaster.broadcast(new CrudMessage(CrudMessage.DML.DELETE, book));
    }

    public void updateBook(Book book) {
        book = bookRepository.save(book);
        Broadcaster.broadcast(new CrudMessage(CrudMessage.DML.UPDATE, book));
    }

    public void insertBook(Book book) {
        book = bookRepository.save(book);
        Broadcaster.broadcast(new CrudMessage(CrudMessage.DML.INSERT, book));
    }
}