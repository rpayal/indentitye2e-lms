package com.co4gsl.indentitye2e_lms;

import com.co4gsl.indentitye2e_lms.controllers.BookController;
import com.co4gsl.indentitye2e_lms.domain.Book;
import com.co4gsl.indentitye2e_lms.dto.BookDTO;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
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
    void testAddNewBookToLibrary() throws JsonProcessingException {
        BookDTO newBook = new BookDTO("isbn-101", "The Lord of the Rings", "J. R. R. Tolkien", 2020, 10);
        addBookToLibraryAndAssert(newBook);
    }

    @Test
    void testConflictForAddNewBookToLibrary_asAlreadyExist() throws JsonProcessingException {
        BookDTO newBook = new BookDTO("isbn-101", "The Lord of the Rings", "J. R. R. Tolkien", 2020, 10);
        addBookToLibraryAndAssert(newBook);

        ResponseEntity<?> response = getAddBookToLibraryResponse(newBook);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Book already exists", response.getBody());
    }

    @Test
    void testAddNewBooksToLibrary_andThenGetItBasedOnAuthor() throws JsonProcessingException {
        String author = "J. R. R. Tolkien";
        BookDTO book1 = new BookDTO("isbn-101", "The Lord of the Rings", author, 2020, 10);
        addBookToLibraryAndAssert(book1);

        BookDTO book2 = new BookDTO("isbn-102", "The Lord of the Rings-2", author, 2022, 10);
        addBookToLibraryAndAssert(book2);
        //
        HttpEntity<BookDTO> entity = new HttpEntity<>(book1, headers);
        ResponseEntity<String> response = restTemplate.getForEntity(
                createURLWithPort("/api/library/author/" + author), String.class);

        assertEquals(200, response.getStatusCode().value());
        List<Book> responseBody = OBJECT_MAPPER.readValue(response.getBody(), new TypeReference<List<Book>>() {
        });
        assertEquals(2, responseBody.size());
    }

    @Test
    void shouldGetNoBooks_forUnknownAuthor() throws JsonProcessingException {
        ResponseEntity<String> response = restTemplate.getForEntity(
                createURLWithPort("/api/library/author/UNKOWN"), String.class);

        assertEquals(200, response.getStatusCode().value());
        List<Book> responseBody = OBJECT_MAPPER.readValue(response.getBody(), new TypeReference<List<Book>>() {
        });
        assertEquals(0, responseBody.size());
    }

    @Test
    void testLibraryManagement_addBook_thenBorrow_thenReturn() throws JsonProcessingException {
        String author = "J. R. R. Tolkien";
        String isbn101 = "isbn-101";
        BookDTO book = new BookDTO(isbn101, "The Lord of the Rings", author, 2020, 10);
        addBookToLibraryAndAssert(book);

        // borrow
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/library/borrow/" + isbn101), HttpMethod.PUT, null, String.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(9, getCachedBook(isbn101).get().getAvailableCopies());

        response = restTemplate.getForEntity(
                createURLWithPort("/api/library/isbn/" + isbn101), String.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(9, OBJECT_MAPPER.readValue(response.getBody(), BookDTO.class).getAvailableCopies());

        // return
        response = restTemplate.exchange(
                createURLWithPort("/api/library/return/" + isbn101), HttpMethod.PUT, null, String.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(10, OBJECT_MAPPER.readValue(response.getBody(), BookDTO.class).getAvailableCopies());
    }

    private void addBookToLibraryAndAssert(BookDTO book1) throws JsonProcessingException {
        ResponseEntity<String> response = getAddBookToLibraryResponse(book1);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(book1, OBJECT_MAPPER.readValue(response.getBody(), BookDTO.class));
    }

    private ResponseEntity<String> getAddBookToLibraryResponse(BookDTO book1) {
        HttpEntity<BookDTO> entity = new HttpEntity<>(book1, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                createURLWithPort("/api/library/add"), entity, String.class);
        return response;
    }


    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    private Optional<BookDTO> getCachedBook(String isbn) {
        return ofNullable(cacheManager.getCache("bookCache")).map(c -> c.get(isbn, BookDTO.class));
    }
}
