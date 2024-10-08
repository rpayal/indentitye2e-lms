package com.co4gsl.indentitye2e_lms.services;

import com.co4gsl.indentitye2e_lms.dto.BookDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface BookService {
    public BookDTO addBook(BookDTO book);

    public void removeBook(String isbn);

    public Optional<BookDTO> findBookByISBN(String isbn);

    public List<BookDTO> findBooksByAuthor(String author);

    public BookDTO borrowBook(String isbn);

    public BookDTO returnBook(String isbn);
}
