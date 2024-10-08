package com.co4gsl.indentitye2e_lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Setter
@Getter
public class BookDTO implements Serializable {
    @NotBlank(message = "ISBN is mandatory")
    @Pattern(
            regexp = "^(97(8|9))?\\d{9}(\\d|X)$",
            message = "ISBN must be a valid 10 or 13 digit number"
    )
    String isbn;
    @NotBlank(message = "Title is mandatory")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    String title;

    @NotBlank(message = "Author is mandatory")
    @Size(max = 50, message = "Author must not exceed 50 characters")
    String author;

    @Positive(message = "Publication year must be a positive number")
    int publicationYear;
    int availableCopies;
}

