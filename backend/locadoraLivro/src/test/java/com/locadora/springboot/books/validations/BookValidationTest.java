package com.locadora.springboot.books.validations;


import com.locadora.locadoraLivro.Books.DTOs.CreateBookRequestDTO;
import com.locadora.locadoraLivro.Books.DTOs.UpdateBookRecordDTO;
import com.locadora.locadoraLivro.Books.Validation.BookValidation;
import com.locadora.locadoraLivro.Books.models.BookModel;
import com.locadora.locadoraLivro.Books.repositories.BookRepository;
import com.locadora.locadoraLivro.Exceptions.CustomValidationException;
import com.locadora.locadoraLivro.Publishers.models.PublisherModel;
import com.locadora.locadoraLivro.Publishers.repositories.PublisherRepository;
import com.locadora.locadoraLivro.Rents.models.RentStatusEnum;
import com.locadora.locadoraLivro.Rents.repositories.RentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookValidationTest {

    @Mock
    BookRepository bookRepository;

    @Mock
    RentRepository rentRepository;

    @Mock
    PublisherRepository publisherRepository;

    @InjectMocks
    BookValidation bookValidation;

    private PublisherModel publisherModel;
    private PublisherModel publisherModelWithIsDeletedTrue;
    private CreateBookRequestDTO createBookRequestDTO;
    private CreateBookRequestDTO createBookWithErrors;
    private CreateBookRequestDTO createBookWithWrongPublisher;
    private UpdateBookRecordDTO updateBookRecordDTO;
    private UpdateBookRecordDTO updateBookRecordWithTotalQuantityLessThanZero;
    private UpdateBookRecordDTO updateBookRecordWithLaunchDateInFuture;
    private BookModel bookModel;

    @BeforeEach
    void setUp(){
        publisherModel = new PublisherModel(1, "Publisher", "publisher@gmail.com", "(85)980028922", "https://teste.com", false);
        publisherModelWithIsDeletedTrue = new PublisherModel(2, "Publisher", "publisher@gmail.com", "(85)980028922", "https://teste.com", true);

        createBookRequestDTO = new CreateBookRequestDTO("Book", "Autor", LocalDate.now(), 3, 1);
        createBookWithErrors = new CreateBookRequestDTO("Book", "Autor", LocalDate.now().plusMonths(1), 3, 2);
        createBookWithWrongPublisher = new CreateBookRequestDTO("Book", "Autor", LocalDate.now(), 3, 2);

        updateBookRecordDTO = new UpdateBookRecordDTO("Book Updated", "Autor", LocalDate.now(), 2, 1);
        updateBookRecordWithTotalQuantityLessThanZero = new UpdateBookRecordDTO("Book Updated", "Autor", LocalDate.now(), -1, 1);
        updateBookRecordWithLaunchDateInFuture = new UpdateBookRecordDTO("Book Updated", "Autor", LocalDate.now().plusMonths(1), 1, 1);

        bookModel = new BookModel(1, "Book", "Autor", LocalDate.now(), 0, 0, false, publisherModel);
    }

    @Test
    void shouldThrowExceptionWhenBookAlreadyExist(){
        List<BookModel> bookList = new ArrayList<>();
        bookList.add(bookModel);
        when(bookRepository.findAllByNameAndIsDeletedFalse(bookModel.getName())).thenReturn(bookList);

        assertThrows(CustomValidationException.class, () -> bookValidation.create(createBookRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenLaunchDateisInTheFuture(){
        assertThrows(CustomValidationException.class, () -> bookValidation.create(createBookWithErrors));
    }

    @Test
    void shouldThrowExceptionWhenTotalQuantityIsEqualsZero(){
        assertThrows(CustomValidationException.class, () -> bookValidation.create(createBookWithErrors));
    }

    @Test
    void shouldThrowExceptionWhenPublisherDoesNotExists(){
        when(publisherRepository.findById(createBookWithWrongPublisher.publisherId())).thenReturn(Optional.ofNullable(publisherModelWithIsDeletedTrue));

        assertThrows(CustomValidationException.class, () -> bookValidation.create(createBookWithWrongPublisher));
    }

    @Test
    void shouldCreateWithValidData(){
        when(publisherRepository.findById(createBookRequestDTO.publisherId())).thenReturn(Optional.ofNullable(publisherModel));
        bookValidation.create(createBookRequestDTO);
    }

    @Test
    void shouldThrowExceptionWhenTotalQuantityIsOLessThanZero(){
        assertThrows(CustomValidationException.class, () -> bookValidation.update(updateBookRecordWithTotalQuantityLessThanZero));
    }

    @Test
    void shouldThrowExceptionWhenLaunchDateIsInFuture(){
        assertThrows(CustomValidationException.class, () -> bookValidation.update(updateBookRecordWithLaunchDateInFuture));
    }

    @Test
    void shouldUpdateWithValidData(){
        bookValidation.update(updateBookRecordDTO);
    }

    @Test
    void shouldDeleteBookWithActiveRent(){
        when(rentRepository.existsByBookIdAndStatus(bookModel.getId(), RentStatusEnum.RENTED)).thenReturn(true);

        assertThrows(CustomValidationException.class, () -> bookValidation.validDeleteBook(bookModel.getId()));
    }

    @Test
    void shouldDeleteBook(){
        when(rentRepository.existsByBookIdAndStatus(bookModel.getId(), RentStatusEnum.RENTED)).thenReturn(false);

        bookValidation.validDeleteBook(bookModel.getId());
    }
}