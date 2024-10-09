package com.co4gsl.indentitye2e_lms;

import com.co4gsl.indentitye2e_lms.controllers.BookController;
import com.co4gsl.indentitye2e_lms.domain.Book;
import com.co4gsl.indentitye2e_lms.dto.BookDTO;
import com.co4gsl.indentitye2e_lms.exceptions.model.ErrorResponse;
import com.co4gsl.indentitye2e_lms.models.JwtRequest;
import com.co4gsl.indentitye2e_lms.models.JwtResponse;
import com.co4gsl.indentitye2e_lms.repositories.BookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static com.co4gsl.indentitye2e_lms.controllers.BookController.CANNT_BE_ADDED_AS_BOOK_ALREADY_EXISTS;
import static com.co4gsl.indentitye2e_lms.controllers.BookController.CANNT_BE_BORROWED_AS_THIS_BOOK_DOES_NOT_EXIST;
import static com.co4gsl.indentitye2e_lms.controllers.BookController.CANNT_BE_RETURNED_AS_THIS_BOOK_DOES_NOT_EXIST;
import static com.co4gsl.indentitye2e_lms.controllers.BookController.LIBRARY_API_DOES_NOT_PERMIT_FURTHER_CALLS;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = LMSApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LMSApplicationTests {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final HttpHeaders headers = new HttpHeaders();
    @Autowired
    BookController bookController;
    @Autowired
    CacheManager cacheManager;
    TestRestTemplate restTemplate = new TestRestTemplate();
    @LocalServerPort
    private int port;
    @Autowired
    private BookRepository bookRepository;

    @BeforeAll
    void setupForAll() throws JsonProcessingException {
        String token = signUpAndGetToken(new JwtRequest("admin@test.com", "123456"));
        headers.set("Authorization", "Bearer " + token);
    }

    @BeforeEach
    void setUp() {
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

        ResponseEntity<String> response = getAddBookToLibraryResponse(newBook);
        assertEquals(409, response.getStatusCode().value());
        assertEquals(CANNT_BE_ADDED_AS_BOOK_ALREADY_EXISTS, getErrorResponse(response.getBody()).getMessage());
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
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/library/author/" + author), HttpMethod.GET, entity, String.class);

        assertEquals(200, response.getStatusCode().value());
        List<Book> responseBody = OBJECT_MAPPER.readValue(response.getBody(), new TypeReference<>() {
        });
        assertEquals(2, responseBody.size());
    }

    @Test
    void shouldGetNoBooks_forUnknownAuthor() throws JsonProcessingException {
        HttpEntity<JwtRequest> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/library/author/UNKNOWN"), HttpMethod.GET, entity, String.class);

        assertEquals(200, response.getStatusCode().value());
        List<Book> responseBody = OBJECT_MAPPER.readValue(response.getBody(), new TypeReference<>() {
        });
        assertEquals(0, responseBody.size());
    }

    @Test
    void shouldGetNoBooks_forRateLimiterReached() throws JsonProcessingException {
        HttpEntity<JwtRequest> entity = new HttpEntity<>(null, headers);
        AtomicReference<ResponseEntity<String>> response = new AtomicReference<>();
        IntStream.range(0, 40).parallel().forEach($ -> response.set(restTemplate.exchange(
                createURLWithPort("/api/library/author/UNKNOWN"), HttpMethod.GET, entity, String.class)));

        assertEquals(429, response.get().getStatusCode().value());
        assertEquals(LIBRARY_API_DOES_NOT_PERMIT_FURTHER_CALLS, getErrorResponse(response.get().getBody()).getMessage());
    }

    @Test
    void testLibraryManagement_addBook_thenBorrow_thenReturn() throws JsonProcessingException {
        String author = "J. R. R. Tolkien";
        String isbn101 = "isbn-101";
        BookDTO book = new BookDTO(isbn101, "The Lord of the Rings", author, 2020, 10);
        addBookToLibraryAndAssert(book);

        // borrow
        HttpEntity<JwtRequest> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/library/borrow/" + isbn101), HttpMethod.PUT, entity, String.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(9, getCachedBook(isbn101).get().getAvailableCopies());

        response = restTemplate.exchange(
                createURLWithPort("/api/library/isbn/" + isbn101), HttpMethod.GET, entity, String.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(9, OBJECT_MAPPER.readValue(response.getBody(), BookDTO.class).getAvailableCopies());

        // return
        response = restTemplate.exchange(
                createURLWithPort("/api/library/return/" + isbn101), HttpMethod.PUT, entity, String.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(10, OBJECT_MAPPER.readValue(response.getBody(), BookDTO.class).getAvailableCopies());
    }

    @Test
    void testBorrowBookFromLibrary_forBookNotExistForISBN() throws JsonProcessingException {
        HttpEntity<JwtRequest> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/library/borrow/UNKNOWN"), HttpMethod.PUT, entity, String.class);

        assertEquals(409, response.getStatusCode().value());
        assertEquals(CANNT_BE_BORROWED_AS_THIS_BOOK_DOES_NOT_EXIST, getErrorResponse(response.getBody()).getMessage());
    }

    @Test
    void testReturnBookFromLibrary_forBookNotExistForISBN() throws JsonProcessingException {
        HttpEntity<JwtRequest> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/library/return/UNKNOWN"), HttpMethod.PUT, entity, String.class);

        assertEquals(409, response.getStatusCode().value());
        assertEquals(CANNT_BE_RETURNED_AS_THIS_BOOK_DOES_NOT_EXIST, getErrorResponse(response.getBody()).getMessage());
    }

    private void addBookToLibraryAndAssert(BookDTO book1) throws JsonProcessingException {
        ResponseEntity<String> response = getAddBookToLibraryResponse(book1);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(book1, OBJECT_MAPPER.readValue(response.getBody(), BookDTO.class));
    }

    private ResponseEntity<String> getAddBookToLibraryResponse(BookDTO book1) {
        HttpEntity<BookDTO> entity = new HttpEntity<>(book1, headers);
        return restTemplate.postForEntity(
                createURLWithPort("/api/library/add"), entity, String.class);
    }

    private ErrorResponse getErrorResponse(String errorResponse) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(errorResponse, ErrorResponse.class);
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    private Optional<BookDTO> getCachedBook(String isbn) {
        return ofNullable(cacheManager.getCache("bookCache")).map(c -> c.get(isbn, BookDTO.class));
    }

    private String signUpAndGetToken(JwtRequest request) throws JsonProcessingException {
        HttpEntity<JwtRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                createURLWithPort("/api/library/auth/signup"), entity, String.class);
        assertEquals(200, response.getStatusCode().value());
        return OBJECT_MAPPER.readValue(response.getBody(), JwtResponse.class).getToken();
    }
}
