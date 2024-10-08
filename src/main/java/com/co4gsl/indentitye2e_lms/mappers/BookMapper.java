package com.co4gsl.indentitye2e_lms.mappers;

import com.co4gsl.indentitye2e_lms.domain.Book;
import com.co4gsl.indentitye2e_lms.dto.BookDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {
    Book toEntity(BookDTO bookDTO);

    BookDTO toDTO(Book book);

    List<BookDTO> toDTOs(List<Book> books);
}
