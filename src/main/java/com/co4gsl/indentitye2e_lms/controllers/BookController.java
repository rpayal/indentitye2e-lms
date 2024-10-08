package com.co4gsl.indentitye2e_lms.controllers;

import com.co4gsl.indentitye2e_lms.dto.BookDTO;
import com.co4gsl.indentitye2e_lms.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping(path = "/add", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addBook(@Validated @RequestBody BookDTO newBook) {
        ResponseEntity<?> result;
        if (bookService.findBookByISBN(newBook.getIsbn()).isPresent()) {
            result = ResponseEntity.status(HttpStatus.CONFLICT).body("Book already exists");
        } else {
            result = ResponseEntity.ok(bookService.addBook(newBook));
        }
        return result;
    }

    @DeleteMapping("/remove/{isbn}")
    public ResponseEntity<String> removeBook(@PathVariable String isbn) {
        if (bookService.findBookByISBN(isbn).isEmpty()) {
            return ResponseEntity.badRequest().body("Book does not exist");
        }
        bookService.removeBook(isbn);
        return ResponseEntity.ok("Book deleted successfully");
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookDTO> findBookForIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(bookService.findBookByISBN(isbn).orElseThrow());
    }

    @GetMapping("/author/{author}")
    public ResponseEntity<Iterable<BookDTO>> findBooksForAuthor(@PathVariable String author) {
        return ResponseEntity.ok(bookService.findBooksByAuthor(author));
    }

    @PutMapping("/borrow/{isbn}")
    public ResponseEntity<?> borrowBook(String isbn) {
        if (bookService.findBookByISBN(isbn).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Can't be borrowed as this book does not exist.");
        }
        return ResponseEntity.ok(bookService.borrowBook(isbn));
    }

    @PutMapping("/return/{isbn}")
    public ResponseEntity<?> returnBook(String isbn) {
        if (bookService.findBookByISBN(isbn).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Can't be returned as this book does not exist.");
        }
        return ResponseEntity.ok(bookService.returnBook(isbn));
    }
}
