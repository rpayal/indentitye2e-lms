package com.co4gsl.indentitye2e_lms.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity
public final class Book {
    @Id
    private String isbn;
    private String title;
    private String author;
    private int publicationYear;
    private int availableCopies;
}
