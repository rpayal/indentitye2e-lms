package com.co4gsl.indentitye2e_lms.services;

import com.co4gsl.indentitye2e_lms.dto.BookDTO;
import com.co4gsl.indentitye2e_lms.repositories.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class BookServiceImplTest {

    @Autowired
    CacheManager cacheManager;
    @Autowired
    private BookService bookService;
    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    public void setUp() {
        bookRepository.deleteAll();
        Objects.requireNonNull(cacheManager.getCache("bookCache")).clear();
    }

    @Test
    void addBook() {
        String newBookIsbn = "isbn101";
        BookDTO book = new BookDTO(newBookIsbn, "The Lord of the Rings", "J. R. R. Tolkien", 2020, 10);
        bookService.addBook(book);
        assertEquals(book, getCachedBook(newBookIsbn).get());

        Optional<BookDTO> savedBook = bookService.findBookByISBN(newBookIsbn);
        assertEquals(book, getCachedBook(newBookIsbn).get());
        assertTrue(savedBook.isPresent());
        assertEquals(book, savedBook.get());
    }

    @Test
    void removeBook() {
        String isbn = "isbn101";
        BookDTO book = new BookDTO(isbn, "The Lord of the Rings", "J. R. R. Tolkien", 2020, 10);
        bookService.addBook(book);
        bookService.removeBook(isbn);

        Optional<BookDTO> savedBook = bookService.findBookByISBN(isbn);
        assertTrue(savedBook.isEmpty());
    }

    @Test
    void findBooksByAuthor() {
        String author = "J. R. R. Tolkien";
        BookDTO book1 = new BookDTO("isbn-101", "The Lord of the Rings", author, 2020, 10);
        bookService.addBook(book1);
        BookDTO book2 = new BookDTO("isbn-102", "The Lord of the Rings-2", author, 2020, 10);
        bookService.addBook(book2);
        BookDTO book3 = new BookDTO("isbn-103", "Guide", "R.K.Lakshman", 1992, 10);
        bookService.addBook(book3);

        List<BookDTO> booksResult = bookService.findBooksByAuthor(author);
        assertEquals(2, booksResult.size());
    }

    @Test
    void borrowBook() {
        String isbnToBorrow = "isbn-101";
        BookDTO book = new BookDTO(isbnToBorrow, "The Lord of the Rings", "J. R. R. Tolkien", 2020, 10);
        bookService.addBook(book);
        assertEquals(book, getCachedBook(isbnToBorrow).get());

        bookService.borrowBook(isbnToBorrow);
        assertEquals(9, getCachedBook(isbnToBorrow).get().getAvailableCopies());

        Optional<BookDTO> savedBook = bookService.findBookByISBN(isbnToBorrow);
        assertTrue(savedBook.isPresent());
        assertEquals(isbnToBorrow, savedBook.get().getIsbn());
        assertEquals(9, savedBook.get().getAvailableCopies());
    }

    @Test
    void returnBook() {
        String isbnToReturn = "isbn-101";
        BookDTO book = new BookDTO(isbnToReturn, "The Lord of the Rings", "J. R. R. Tolkien", 2020, 10);
        bookService.addBook(book);
        assertEquals(book, getCachedBook(isbnToReturn).get());

        bookService.borrowBook(isbnToReturn);
        assertEquals(9, getCachedBook(isbnToReturn).get().getAvailableCopies());

        bookService.returnBook(isbnToReturn);
        assertEquals(10, getCachedBook(isbnToReturn).get().getAvailableCopies());

        Optional<BookDTO> savedBook = bookService.findBookByISBN(isbnToReturn);
        assertTrue(savedBook.isPresent());
        assertEquals(isbnToReturn, savedBook.get().getIsbn());
        assertEquals(10, savedBook.get().getAvailableCopies());
    }

    private Optional<BookDTO> getCachedBook(String isbn) {
        return ofNullable(cacheManager.getCache("bookCache")).map(c -> c.get(isbn, BookDTO.class));
    }
}