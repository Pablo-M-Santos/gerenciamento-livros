package com.locadora.locadoraLivro.Books.Validation;

import com.locadora.locadoraLivro.Books.DTOs.CreateBookRequestDTO;
import com.locadora.locadoraLivro.Books.DTOs.UpdateBookRecordDTO;
import com.locadora.locadoraLivro.Books.repositories.BookRepository;
import com.locadora.locadoraLivro.Exceptions.CustomValidationException;
import com.locadora.locadoraLivro.Publishers.models.PublisherModel;
import com.locadora.locadoraLivro.Publishers.repositories.PublisherRepository;
import com.locadora.locadoraLivro.Rents.models.RentStatusEnum;
import com.locadora.locadoraLivro.Rents.repositories.RentRepository;
import com.locadora.locadoraLivro.Users.DTOs.CreateUserRequestDTO;
import com.locadora.locadoraLivro.Users.DTOs.UpdateUserRequestDTO;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@AllArgsConstructor
@Component
public class BookValidation {

    @Autowired
    BookRepository bookRepository;

    @Autowired
    RentRepository rentRepository;

    @Autowired
    PublisherRepository publisherRepository;

    public void create(CreateBookRequestDTO data) {
        validName(data);
        validAuthor(data);
        validLaunchDate(data);
        validTotalQuantity(data);
        validPublisherExist(data);
    }

    public void update(UpdateBookRecordDTO data, int id) {
        validNameUpdate(data, id);
        validAuthorUpdate(data, id);
        validLaunchDateUpdate(data, id);
        validTotalQuantityUpdate(data, id);
    }

    public void validName(CreateBookRequestDTO data) {
        if (data.name() == null || data.name().trim().isEmpty()) {
            throw new CustomValidationException("O nome do livro não pode ser vazio ou nulo");
        }
    }

    public void validNameUpdate(UpdateBookRecordDTO data, int id) {
        if (data.name() == null || data.name().trim().isEmpty()) {
            throw new CustomValidationException("O nome do livro não pode ser vazio ou nulo");
        }
    }

    public void validAuthor(CreateBookRequestDTO data) {
        if (data.author() == null || data.author().trim().isEmpty()) {
            throw new CustomValidationException("O autor do livro não pode ser vazio ou nulo");
        }
    }

    public void validAuthorUpdate(UpdateBookRecordDTO data, int id) {
        if (data.author() == null || data.author().trim().isEmpty()) {
            throw new CustomValidationException("O autor do livro não pode ser vazio ou nulo");
        }
    }

    public void validLaunchDate(CreateBookRequestDTO data) {
        if (data.launchDate() == null) {
            throw new CustomValidationException("A data de lançamento não pode ser nula");
        }
        if (data.launchDate().isAfter(LocalDate.now())) {
            throw new CustomValidationException("A data de lançamento não pode ser no futuro");
        }
    }

    public void validLaunchDateUpdate(UpdateBookRecordDTO data, int id) {
        if (data.launchDate() == null) {
            throw new CustomValidationException("A data de lançamento não pode ser nula");
        }
        if (data.launchDate().isAfter(LocalDate.now())) {
            throw new CustomValidationException("A data de lançamento não pode ser no futuro");
        }
    }

    public void validTotalQuantity(CreateBookRequestDTO data) {
        if (data.totalQuantity() < 1) {
            throw new CustomValidationException("A quantidade total deve ser maior ou igual a 1");
        }
    }

    public void validTotalQuantityUpdate(UpdateBookRecordDTO data, int id) {
        if (data.totalQuantity() < 1) {
            throw new CustomValidationException("A quantidade total deve ser maior ou igual a 1");
        }
    }


    public void validPublisherExist(CreateBookRequestDTO data) {
        PublisherModel publisher = publisherRepository.findById(data.publisherId())
                .orElseThrow(() -> new CustomValidationException("O editor não existe"));

        if (publisher.isDeleted()) {
            throw new CustomValidationException("O editor não existe");
        }
    }


    public void validDeleteBook(int id) {
        boolean hasActiveRent = rentRepository.existsByBookIdAndStatus(id, RentStatusEnum.ALUGADO);
        if (hasActiveRent) {
            throw new CustomValidationException("O livro não pode ser excluído porque possui uma locação ativa");
        }
    }
}
