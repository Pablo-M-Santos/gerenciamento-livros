package com.locadora.locadoraLivro.Publishers.services;

import com.locadora.locadoraLivro.Exceptions.ModelNotFoundException;
import com.locadora.locadoraLivro.Publishers.DTOs.CreatePublisherRequestDTO;
import com.locadora.locadoraLivro.Publishers.DTOs.UpdatePublisherRecordDTO;
import com.locadora.locadoraLivro.Publishers.Validation.PublisherValidation;
import com.locadora.locadoraLivro.Publishers.models.PublisherModel;
import com.locadora.locadoraLivro.Publishers.repositories.PublisherRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PublisherServices {
    @Autowired
    PublisherRepository publisherRepository;

    @Autowired
    PublisherValidation publisherValidation;

    public ResponseEntity<Void> create(@Valid CreatePublisherRequestDTO data) {

        publisherValidation.validateName(data);
        publisherValidation.validateEmail(data);
        publisherValidation.validateTelephone(data);
        publisherValidation.validateSite(data);

        PublisherModel newPublisher = new PublisherModel(data.name(), data.email(), data.telephone(), data.site());
        publisherRepository.save(newPublisher);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public Page<PublisherModel> findAll(String search, int page) {
        int size = 8;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        if (Objects.equals(search, "")) {
            Page<PublisherModel> publishers = publisherRepository.findAllByIsDeletedFalse(pageable);
            if (publishers.isEmpty()) throw new ModelNotFoundException("No publishers found");
            return publishers;
        } else {
            Page<PublisherModel> publisherSearch = publisherRepository.findAllByKeyword(search, pageable);
            if (publisherSearch.isEmpty()) throw new ModelNotFoundException("No publishers found for the given search term");
            return publisherSearch;
        }
    }

    public List<PublisherModel> findAllWithoutPagination(String search) {
        if (Objects.equals(search, "")) {
            return publisherRepository.findAllByIsDeletedFalse(Sort.by(Sort.Direction.DESC, "id"));
        } else {
            return publisherRepository.findAllByKeyword(Sort.by(Sort.Direction.DESC, "id"), search);
        }
    }


    public Optional<PublisherModel> findById(int id) {
        return publisherRepository.findById(id);
    }

    public ResponseEntity<Object> update(int id, @Valid UpdatePublisherRecordDTO updatePublisherRecordDTO) {
        Optional<PublisherModel> response = publisherRepository.findById(id);
        if (response.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Publisher not found");

        publisherValidation.validateNameUpdate(updatePublisherRecordDTO, id);
        publisherValidation.validateEmailUpdate(updatePublisherRecordDTO, id);
        publisherValidation.validateTelephoneUpdate(updatePublisherRecordDTO, id);
        publisherValidation.validateSiteUpdate(updatePublisherRecordDTO, id);

        var publisherModel = response.get();
        BeanUtils.copyProperties(updatePublisherRecordDTO, publisherModel);

        return ResponseEntity.status(HttpStatus.OK).body(publisherRepository.save(publisherModel));
    }

    public ResponseEntity<Object> delete(int id) {
        Optional<PublisherModel> response = publisherRepository.findById(id);
        if (response.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Publisher not found");

        publisherValidation.validateDeletePublisher(id);

        PublisherModel publisher = response.get();

        publisher.setDeleted(true);

        publisherRepository.save(publisher);

        return ResponseEntity.status(HttpStatus.OK).body("Publisher deleted successfully");
    }


}