package com.locadora.locadoraLivro.Publishers.services;

import com.locadora.locadoraLivro.Books.models.BookModel;
import com.locadora.locadoraLivro.Books.repositories.BookRepository;
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

    @Autowired
    BookRepository bookRepository;

    public ResponseEntity<Void> create(@Valid CreatePublisherRequestDTO data) {

        publisherValidation.create(data);
        Optional<PublisherModel> existingPublisher = publisherRepository.findByNameOrEmailAndIsDeletedTrue(data.name(), data.email());

        if (existingPublisher.isPresent()) {
            PublisherModel publisher = existingPublisher.get();
            publisher.setIsDeleted(false);
            publisherRepository.save(publisher);
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            PublisherModel newPublisher = new PublisherModel(data.name(), data.email(), data.telephone(), data.site());
            publisherRepository.save(newPublisher);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }


    public Page<PublisherModel> findAll(String search, int page) {
        int size = 8;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        search = search.trim().replaceAll("[^0-9]", "");

        if (search.isEmpty()) {
            Page<PublisherModel> publishers = publisherRepository.findAllByIsDeletedFalse(pageable);
            if (publishers.isEmpty()) throw new ModelNotFoundException();
            return publishers;
        } else {
            Page<PublisherModel> publisherSearch = publisherRepository.findAllByName(search, pageable);
            return publisherSearch;
        }
    }

    public List<PublisherModel> findAllWithoutPagination(String search) {
        // Remove todos os caracteres não numéricos
        search = search.trim().replaceAll("[^0-9]", "");

        if (search.isEmpty()) {
            return publisherRepository.findAllByIsDeletedFalse(Sort.by(Sort.Direction.DESC, "id"));
        } else {
            return publisherRepository.findAllByName(search, Sort.by(Sort.Direction.DESC, "id"));
        }
    }



    public Optional<PublisherModel> findById(int id){
        return publisherRepository.findById(id);
    }

    public ResponseEntity<Object> update(int id, @Valid UpdatePublisherRecordDTO updatePublisherRecordDTO){
        Optional<PublisherModel> response = publisherRepository.findById(id);
        if (response.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Editora não encontrada.");

        publisherValidation.update(updatePublisherRecordDTO, id);

        var publisherModel = response.get();
        BeanUtils.copyProperties(updatePublisherRecordDTO, publisherModel);

        return ResponseEntity.status(HttpStatus.OK).body(publisherRepository.save(publisherModel));
    }

    public ResponseEntity<Object> delete(int id) {
        Optional<PublisherModel> publisher = publisherRepository.findById(id);
        if (publisher.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Editora não encontrada.");
        }

        // Verificar se há livros associados à editora que não foram excluídos
        List<BookModel> activeBooks = bookRepository.findByPublisherIdAndIsDeletedFalse(id);
        if (!activeBooks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Não é possível excluir a editora. Existem livros ativos associados.");
        }

        // Realizar a exclusão lógica da editora
        PublisherModel publisherModel = publisher.get();
        publisherModel.setIsDeleted(true);  // Use o método correto aqui
        publisherRepository.save(publisherModel);  // Salvar a editora atualizada

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();  // Sucesso na exclusão lógica
    }

}