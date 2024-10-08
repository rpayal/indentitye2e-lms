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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    Logger LOGGER = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BookDTO addBook(BookDTO book) {
        LOGGER.info("Adding book !!");
        return bookMapper.toDTO(bookRepository.save(bookMapper.toEntity(book)));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"bookCache"}, key = "#p0")
    public void removeBook(String isbn) {
        bookRepository.deleteById(isbn);
    }

    @Override
    @Transactional
    @Cacheable(cacheNames = {"bookCache"}, key = "#p0", unless = "#result == null")
    public Optional<BookDTO> findBookByISBN(String isbn) {
        Optional<Book> book = bookRepository.findById(isbn);
        return book.isPresent() ? Optional.of(bookMapper.toDTO(book.get())) : Optional.empty();
    }

    @Override
    @Transactional
    @Cacheable(cacheNames = {"authorBooks"}, key = "#p0")
    public List<BookDTO> findBooksByAuthor(String author) {
        return bookMapper.toDTOs(bookRepository.findByAuthor(author));
    }

    @Override
    @Transactional
    @CachePut(cacheNames = {"bookCache"}, key = "#p0")
    public BookDTO borrowBook(String isbn) {
        Book book = bookRepository.findById(isbn).orElseThrow();
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        return bookMapper.toDTO(bookRepository.save(book));
    }

    @Override
    @Transactional
    @CachePut(cacheNames = {"bookCache"}, key = "#p0")
    public BookDTO returnBook(String isbn) {
        Book book = bookRepository.findById(isbn).orElseThrow();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        return bookMapper.toDTO(bookRepository.save(book));
    }
}
