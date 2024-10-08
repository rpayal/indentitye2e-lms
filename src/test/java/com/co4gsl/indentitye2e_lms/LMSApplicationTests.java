package com.co4gsl.indentitye2e_lms;

import com.co4gsl.indentitye2e_lms.controllers.BookController;
import com.co4gsl.indentitye2e_lms.domain.Book;
import com.co4gsl.indentitye2e_lms.repositories.BookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = LMSApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LMSApplicationTests {

    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Autowired
    BookController bookController;
    @Autowired
    CacheManager cacheManager;
    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpHeaders headers = new HttpHeaders();
    @LocalServerPort
    private int port;
    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    public void setUp() {
        bookRepository.deleteAll();
        Objects.requireNonNull(cacheManager.getCache("bookCache")).clear();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldAddNewBookToLibrary() throws JsonProcessingException {
        Book newBook = new Book("isbn-101", "The Lord of the Rings", "J. R. R. Tolkien", 2020, 10);
        addBookToLibraryAndAssert(newBook);
    }

    @Test
    void shouldConflictWhileAddNewBookToLibrary_alreadyExist() throws JsonProcessingException {
        Book newBook = new Book("isbn-101", "The Lord of the Rings", "J. R. R. Tolkien", 2020, 10);
        addBookToLibraryAndAssert(newBook);

        ResponseEntity<String> response = getAddBookToLibraryResponse(newBook);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Book already exists", response.getBody());
    }

    @Test
    void shouldAddNewBookToLibrary_andThenGetItBasedOnAuthor() throws JsonProcessingException {
        String author = "J. R. R. Tolkien";
        Book book1 = new Book("isbn-101", "The Lord of the Rings", author, 2020, 10);
        addBookToLibraryAndAssert(book1);

        Book book2 = new Book("isbn-102", "The Lord of the Rings-2", author, 2022, 10);
        addBookToLibraryAndAssert(book2);
        //
        HttpEntity<Book> entity = new HttpEntity<>(book1, headers);
        ResponseEntity<String> response = restTemplate.getForEntity(
                createURLWithPort("/api/library/author/" + author), String.class);

        assertEquals(200, response.getStatusCode().value());
        List<Book> responseBody = OBJECT_MAPPER.readValue(response.getBody(), new TypeReference<List<Book>>() {
        });
        assertEquals(2, responseBody.size());
    }

    private void addBookToLibraryAndAssert(Book book1) throws JsonProcessingException {
        ResponseEntity<String> response = getAddBookToLibraryResponse(book1);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(book1, OBJECT_MAPPER.readValue(response.getBody(), Book.class));
    }

    private ResponseEntity<String> getAddBookToLibraryResponse(Book book1) {
        HttpEntity<Book> entity = new HttpEntity<>(book1, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                createURLWithPort("/api/library/add"), entity, String.class);
        return response;
    }


    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
}
