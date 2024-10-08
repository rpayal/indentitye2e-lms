package com.co4gsl.indentitye2e_lms.services;

import com.co4gsl.indentitye2e_lms.domain.Book;
import com.co4gsl.indentitye2e_lms.dto.BookDTO;
import com.co4gsl.indentitye2e_lms.mappers.BookMapper;
import com.co4gsl.indentitye2e_lms.repositories.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    Logger LOGGER = LoggerFactory.getLogger(BookServiceImpl.class);

    @Autowired
    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    @CachePut(cacheNames = {"bookCache"}, key = "#book.isbn")
    public BookDTO addBook(BookDTO book) {
        LOGGER.info("Adding book !!");
        return bookMapper.toDTO(bookRepository.save(bookMapper.toEntity(book)));
    }

    @Override
    @CacheEvict(cacheNames = {"bookCache"}, key = "#isbn")
    public void removeBook(String isbn) {
        bookRepository.deleteById(isbn);
    }

    @Override
    @Cacheable(cacheNames = {"bookCache"}, key = "#isbn")
    public Optional<BookDTO> findBookByISBN(String isbn) {
        Optional<Book> book = bookRepository.findById(isbn);
        return book.isPresent() ? Optional.of(bookMapper.toDTO(book.get())) : Optional.empty();
    }

    @Override
    @Cacheable(cacheNames = {"authorBooks"}, key = "#author")
    public List<BookDTO> findBooksByAuthor(String author) {
        return bookMapper.toDTOs(bookRepository.findByAuthor(author));
    }

    @Override
    @CachePut(cacheNames = {"bookCache"}, key = "#isbn")
    public BookDTO borrowBook(String isbn) {
        Book book = bookMapper.toEntity(findBookByISBN(isbn).orElseThrow());
        bookRepository.updateBookAvailableCopiesByIsbn(book.getAvailableCopies() - 1, book.getIsbn());
        return bookMapper.toDTO(bookRepository.findById(isbn).get());
    }

    @Override
    @CachePut(cacheNames = {"bookCache"}, key = "#isbn")
    public BookDTO returnBook(String isbn) {
        Book book = bookMapper.toEntity(findBookByISBN(isbn).orElseThrow());
        bookRepository.updateBookAvailableCopiesByIsbn(book.getAvailableCopies() + 1, book.getIsbn());
        return bookMapper.toDTO(bookRepository.findById(isbn).get());
    }
}
