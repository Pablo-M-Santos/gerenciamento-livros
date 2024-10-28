package com.locadora.locadoraLivro.Rents.services;

import com.locadora.locadoraLivro.Books.models.BookModel;
import com.locadora.locadoraLivro.Books.repositories.BookRepository;
import com.locadora.locadoraLivro.Exceptions.ModelNotFoundException;
import com.locadora.locadoraLivro.Renters.models.RenterModel;
import com.locadora.locadoraLivro.Renters.repositories.RenterRepository;
import com.locadora.locadoraLivro.Rents.DTOs.CreateRentRequestDTO;
import com.locadora.locadoraLivro.Rents.DTOs.UpdateRentRecordDTO;
import com.locadora.locadoraLivro.Rents.Validation.RentValidation;
import com.locadora.locadoraLivro.Rents.models.RentModel;
import com.locadora.locadoraLivro.Rents.models.RentStatusEnum;
import com.locadora.locadoraLivro.Rents.repositories.RentRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class RentServices {

    @Autowired
    RentRepository rentRepository;

    @Autowired
    RenterRepository renterRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    RentValidation rentValidation;

    public ResponseEntity<Void> create(@Valid CreateRentRequestDTO data) {
        rentValidation.validateRenterId(data);
        rentValidation.validateRentRepeated(data);
        rentValidation.validateRentLate(data);

        RenterModel renter = renterRepository.findById(data.renterId()).get();

        rentValidation.validateBookId(data);

        BookModel book = bookRepository.findById(data.bookId()).get();

        rentValidation.validateDeadLine(data);

        RentModel newRent = new RentModel(renter, book, data.deadLine(), LocalDate.now());
        newRent.setStatus(RentStatusEnum.ALUGADO);
        rentRepository.save(newRent);

        rentValidation.validateBookTotalQuantity(book);

        book.setTotalQuantity(book.getTotalQuantity() - 1);
        bookRepository.save(book);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public Page<RentModel> findAll(String search, int page) {
        int size = 5;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        if (Objects.equals(search, "")) {
            Page<RentModel> rents = rentRepository.findAll(pageable);
            if (rents.isEmpty()) throw new ModelNotFoundException();
            for (RentModel rent : rents) {
                rentValidation.setRentStatus(rent);
            }
            return rents;
        } else {
            return rentRepository.findAllByRenterNameOrBookName(search, pageable);
        }
    }


    public List<RentModel> findAllWithoutPagination(String search) {
        if (Objects.equals(search, "")) {
            return rentRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        } else {
            return rentRepository.findAllByRenterNameOrBookName(search, Sort.by(Sort.Direction.DESC, "id"));
        }
    }

    public RentModel findById(int id) {
        return rentRepository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException("Rent not found with ID: " + id));
    }


    public ResponseEntity<Object> delivered(int id) {
        Optional<RentModel> optionalRent = rentRepository.findById(id);
        if (optionalRent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rent not found");
        }

        RentModel rent = optionalRent.get();

        rent.setDevolutionDate(LocalDate.now());

        rentValidation.deliveredValidate(id);
        rentValidation.setRentStatus(rent);

        rentRepository.save(rent);
        return ResponseEntity.status(HttpStatus.OK).body(rent);
    }

    public ResponseEntity<Object> update(int id, @Valid UpdateRentRecordDTO updateRentRecordDTO) {
        Optional<RentModel> rentOptional = rentRepository.findById(id);
        if (rentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rent not found");
        }

        rentValidation.validateRenterIdUpdate(updateRentRecordDTO);
        RenterModel renter = renterRepository.findById(updateRentRecordDTO.renterId()).get();

        rentValidation.validateBookIdUpdate(updateRentRecordDTO);
        BookModel book = bookRepository.findById(updateRentRecordDTO.bookId()).get();

        rentValidation.validateDeadLineUpdate(updateRentRecordDTO, id);
        rentValidation.validateBookTotalQuantity(book);

        RentModel rentModel = rentOptional.get();
        rentModel.setBook(book);
        rentModel.setRenter(renter);
        rentModel.setDeadLine(updateRentRecordDTO.deadLine());

        rentRepository.save(rentModel);

        return ResponseEntity.status(HttpStatus.OK).body("Rent updated successfully");
    }
}