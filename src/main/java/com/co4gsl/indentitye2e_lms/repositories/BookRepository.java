package com.co4gsl.indentitye2e_lms.repositories;

import com.co4gsl.indentitye2e_lms.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

    List<Book> findByAuthor(String author);

    @Transactional
    @Modifying
    @Query("update Book b set b.availableCopies = ?1 where b.isbn = ?2")
    void updateBookAvailableCopiesByIsbn(int updatedAvailableCopies, String isbn);
}
