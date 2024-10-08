package com.co4gsl.indentitye2e_lms.controllers;

import com.co4gsl.indentitye2e_lms.dto.BookDTO;
import com.co4gsl.indentitye2e_lms.exceptions.AppExceptionHandler;
import com.co4gsl.indentitye2e_lms.exceptions.BookAlreadyExistException;
import com.co4gsl.indentitye2e_lms.exceptions.BookNotFoundException;
import com.co4gsl.indentitye2e_lms.exceptions.ConflictException;
import com.co4gsl.indentitye2e_lms.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class BookController extends AppExceptionHandler {

    public static final String CANNT_BE_RETURNED_AS_THIS_BOOK_DOES_NOT_EXIST = "Can't be returned as this book does not exist.";
    public static final String CANNT_BE_BORROWED_AS_THIS_BOOK_DOES_NOT_EXIST = "Can't be borrowed as this book does not exist.";
    public static final String CANNT_BE_ADDED_AS_BOOK_ALREADY_EXISTS = "Can't be added as book already exists";
    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping(path = "/add", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addBook(@Validated @RequestBody BookDTO newBook) {
        ResponseEntity<?> result;
        if (bookService.findBookByISBN(newBook.getIsbn()).isPresent()) {
            throw new BookAlreadyExistException(CANNT_BE_ADDED_AS_BOOK_ALREADY_EXISTS);
        } else {
            result = ResponseEntity.ok(bookService.addBook(newBook));
        }
        return result;
    }

    @DeleteMapping("/remove/{isbn}")
    public ResponseEntity<String> removeBook(@PathVariable String isbn) {
        if (bookService.findBookByISBN(isbn).isEmpty()) {
            throw new BookNotFoundException(CANNT_BE_BORROWED_AS_THIS_BOOK_DOES_NOT_EXIST);
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
    public ResponseEntity<BookDTO> borrowBook(@PathVariable String isbn) {
        if (bookService.findBookByISBN(isbn).isEmpty()) {
            throw new ConflictException(CANNT_BE_BORROWED_AS_THIS_BOOK_DOES_NOT_EXIST);
        }
        return ResponseEntity.ok(bookService.borrowBook(isbn));
    }

    @PutMapping("/return/{isbn}")
    public ResponseEntity<BookDTO> returnBook(@PathVariable String isbn) {
        if (bookService.findBookByISBN(isbn).isEmpty()) {
            throw new ConflictException(CANNT_BE_RETURNED_AS_THIS_BOOK_DOES_NOT_EXIST);
        }
        return ResponseEntity.ok(bookService.returnBook(isbn));
    }
}
