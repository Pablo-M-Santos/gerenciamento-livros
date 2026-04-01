package com.locadora.locadoraLivro.Renters.services;

import com.locadora.locadoraLivro.Renters.DTOs.CreateRenterRequestDTO;
import com.locadora.locadoraLivro.Renters.DTOs.UpdateRenterRequestDTO;
import com.locadora.locadoraLivro.Renters.Validation.RenterValidation;
import com.locadora.locadoraLivro.Renters.models.RenterModel;
import com.locadora.locadoraLivro.Renters.repositories.RenterRepository;
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
import java.util.Optional;

@Service
public class RenterServices {

    @Autowired
    private RenterRepository renterRepository;

    @Autowired
    private RenterValidation renterValidation;

    public ResponseEntity<Void> create(@Valid CreateRenterRequestDTO data) {
        Optional<RenterModel> existingRenter = renterRepository.findByCpfAndIsDeletedTrue(data.cpf());

        if (existingRenter.isPresent()) {
            RenterModel renter = existingRenter.get();
            renter.setDeleted(false);
            renter.setName(data.name());
            renter.setEmail(data.email());
            renter.setTelephone(data.telephone());
            renter.setAddress(data.address());
            // Atualiza o CPF, se necessário
            renterRepository.save(renter);

            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            renterValidation.create(data);

            RenterModel newRenter = new RenterModel(data.name(), data.email(), data.telephone(), data.address(), data.cpf());
            renterRepository.save(newRenter);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }


    public Page<RenterModel> findAll(String search, int page) {
        int size = 8;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        if (search == null || search.isBlank()) {
            return renterRepository.findAllByIsDeletedFalse(pageable);
        } else {
            return renterRepository.findAllBySearchTerm(search, pageable);
        }
    }

    public List<RenterModel> findAllWithoutPagination(String search) {
        if (search == null || search.isBlank()) {
            return renterRepository.findAllByIsDeletedFalse(Sort.by(Sort.Direction.DESC, "id"));
        } else {
            return renterRepository.findAllBySearchTerm(search, Sort.by(Sort.Direction.DESC, "id"));
        }
    }


    public Optional<RenterModel> findById(int id) {
        return renterRepository.findById(id);
    }

    public ResponseEntity<Object> update(int id, @Valid UpdateRenterRequestDTO updateRenterRequestDTO) {
        Optional<RenterModel> response = renterRepository.findById(id);
        if (response.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Locatário não encontrado.");

        renterValidation.update(updateRenterRequestDTO, id);

        RenterModel renterModel = response.get();
        BeanUtils.copyProperties(updateRenterRequestDTO, renterModel);

        return ResponseEntity.status(HttpStatus.OK).body(renterRepository.save(renterModel));
    }

    public ResponseEntity<Object> delete(int id) {
        Optional<RenterModel> response = renterRepository.findById(id);
        if (response.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Locatário não encontrado.");

        renterValidation.validateDeleteRenter(id);

        RenterModel renter = response.get();

        renter.setDeleted(true);

        renterRepository.save(renter);

        return ResponseEntity.status(HttpStatus.OK).body("Locatário excluído com sucesso.");
    }

    public long getActiveRentersCount() {
        return renterRepository.countActiveRenters();
    }

    public long getDeletedRentersCount() {
        return renterRepository.countDeletedRenters();
    }

    public long getTotalRentersCount() {
        return renterRepository.countTotalRenters();
    }
}
