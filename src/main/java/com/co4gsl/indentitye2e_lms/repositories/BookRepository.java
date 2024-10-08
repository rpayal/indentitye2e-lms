package com.co4gsl.indentitye2e_lms.repositories;

import com.co4gsl.indentitye2e_lms.domain.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Book save(Book book);

    @Lock(LockModeType.PESSIMISTIC_READ)
    void deleteById(String isbn);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Book> findById(String isbn);

    @Lock(LockModeType.PESSIMISTIC_READ)
    List<Book> findByAuthor(String author);
}
