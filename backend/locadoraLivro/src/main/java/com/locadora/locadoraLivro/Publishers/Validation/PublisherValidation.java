package com.locadora.locadoraLivro.Publishers.Validation;

import com.locadora.locadoraLivro.Books.repositories.BookRepository;
import com.locadora.locadoraLivro.Exceptions.CustomValidationException;
import com.locadora.locadoraLivro.Publishers.DTOs.CreatePublisherRequestDTO;
import com.locadora.locadoraLivro.Publishers.DTOs.UpdatePublisherRecordDTO;
import com.locadora.locadoraLivro.Publishers.models.PublisherModel;
import com.locadora.locadoraLivro.Publishers.repositories.PublisherRepository;
import com.locadora.locadoraLivro.Rents.models.RentStatusEnum;
import com.locadora.locadoraLivro.Rents.repositories.RentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@Component
public class PublisherValidation {

    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;
    private final RentRepository rentRepository;

    public void create(CreatePublisherRequestDTO data) {
        validateName(data);
        validateEmail(data);
        validateTelephone(data);
        validateSite(data);
    }

    public void update(UpdatePublisherRecordDTO data, int id) {
        validateNameUpdate(data, id);
        validateEmailUpdate(data, id);
        validateTelephoneUpdate(data, id);
        validateSiteUpdate(data, id);
    }

    // Criação validações de Nome
    public void validateName(CreatePublisherRequestDTO data) {
        if (data.name() == null || data.name().trim().isEmpty()) {
            throw new CustomValidationException("O nome da editora não pode estar vazio.");
        }

        if (publisherRepository.findByNameAndIsDeletedFalse(data.name()) != null) {
            throw new CustomValidationException("Nome da editora já em uso.");
        }
    }

    // Atualização validações de Nome
    public void validateNameUpdate(UpdatePublisherRecordDTO data, int id) {
        Optional<PublisherModel> publisherOptional = publisherRepository.findById(id);

        if (publisherOptional.isEmpty()) {
            throw new CustomValidationException("Editora não encontrada.");
        }

        PublisherModel publisherModel = publisherOptional.get();

        if (!Objects.equals(publisherModel.getName(), data.name())) {
            if (publisherRepository.findByName(data.name()) != null) {
                throw new CustomValidationException("Nome da editora já em uso.");
            }
        }
    }

    // Criação validações de email
    public void validateEmail(CreatePublisherRequestDTO data) {
        if (data.email() == null || data.email().trim().isEmpty()) {
            throw new CustomValidationException("O e-mail não pode estar vazio.");
        }

        if (publisherRepository.findByEmail(data.email()) != null) {
            throw new CustomValidationException("E-mail já em uso.");
        }
    }

    public void validateEmailUpdate(UpdatePublisherRecordDTO data, int id) {
        Optional<PublisherModel> publisherOptional = publisherRepository.findById(id);

        if (publisherOptional.isEmpty()) {
            throw new CustomValidationException("Editora não encontrada.");
        }

        PublisherModel publisherModel = publisherOptional.get();

        if (!Objects.equals(publisherModel.getEmail(), data.email())) {
            if (publisherRepository.findByEmail(data.email()) != null) {
                throw new CustomValidationException("E-mail já em uso.");
            }
        }
    }

    // Criação validações de telefone
    public void validateTelephone(CreatePublisherRequestDTO data) {
        if (data.telephone() == null || data.telephone().trim().isEmpty()) {
            throw new CustomValidationException("O telefone não pode estar vazio.");
        }

        if (publisherRepository.findByTelephone(data.telephone()) != null) {
            throw new CustomValidationException("Este telefone já está em uso.");
        }
    }

    public void validateTelephoneUpdate(UpdatePublisherRecordDTO data, int id) {
        Optional<PublisherModel> publisherOptional = publisherRepository.findById(id);

        if (publisherOptional.isEmpty()) {
            throw new CustomValidationException("Editora não encontrada.");
        }

        PublisherModel publisherModel = publisherOptional.get();

        if (!Objects.equals(publisherModel.getTelephone(), data.telephone())) {
            if (publisherRepository.findByTelephone(data.telephone()) != null) {
                throw new CustomValidationException("Este telefone já está em uso.");
            }
        }
    }

    // Criação validações de site
    public void validateSite(CreatePublisherRequestDTO data) {
        if (data.site() != null && !data.site().trim().isEmpty()) {
            if (publisherRepository.findBySite(data.site()) != null) {
                throw new CustomValidationException("Este site já está em uso.");
            }
        }
    }

    public void validateSiteUpdate(UpdatePublisherRecordDTO data, int id) {
        Optional<PublisherModel> publisherOptional = publisherRepository.findById(id);

        if (publisherOptional.isEmpty()) {
            throw new CustomValidationException("Editora não encontrada.");
        }

        PublisherModel publisherModel = publisherOptional.get();

        if (!Objects.equals(publisherModel.getSite(), data.site())) {
            if (publisherRepository.findBySite(data.site()) != null) {
                throw new CustomValidationException("Este site já está em uso.");
            }
        }
    }


    public void delete(int id) {
        Optional<PublisherModel> publisherOptional = publisherRepository.findById(id);
        if (publisherOptional.isEmpty()) {
            throw new CustomValidationException("Editora não encontrada.");
        }

        validateDeletePublisher(id);

        publisherRepository.deleteById(id);
    }



    public void validateDeletePublisher(int id) {
        var books = bookRepository.findByPublisherId(id);
        for (var book : books) {
            if (rentRepository.existsByBookIdAndStatus(book.getId(), RentStatusEnum.ALUGADO)) {
                throw new CustomValidationException("Não é possível excluir o editor. Existem livros atualmente alugados.");
            }
        }
    }

}
