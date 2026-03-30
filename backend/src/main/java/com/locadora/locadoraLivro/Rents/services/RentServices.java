package com.locadora.locadoraLivro.Rents.services;

import com.locadora.locadoraLivro.Books.models.BookModel;
import com.locadora.locadoraLivro.Books.repositories.BookRepository;
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
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
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

    public ResponseEntity<Void> create(@Valid CreateRentRequestDTO data){
        RenterModel renter = renterRepository.findById(data.renterId()).get();

        BookModel book = bookRepository.findById(data.bookId()).get();

        RentModel newRent = new RentModel(renter, book, data.deadLine());

        rentValidation.create(data);

        newRent.setStatus(RentStatusEnum.RENTED);
        rentRepository.save(newRent);

        rentValidation.validateBookTotalQuantity(book);

        book.setTotalQuantity(book.getTotalQuantity() - 1);
        bookRepository.save(book);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public Page<RentModel> findAll(String search, int page) {
        int size = 8;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        if (search == null || search.isBlank()) {

            Page<RentModel> rents = rentRepository.findAll(pageable);

            for (RentModel rent : rents) {
                rentValidation.setRentStatus(rent);
            }
            return rents;

        } else {

            if (search.equalsIgnoreCase("Não entregue")) {

                return rentRepository.findAllByDevolutionDateIsNull(pageable);
            } else {

                return rentRepository.findAllBySearch(search, pageable);
            }
        }
    }



    public Page<RentModel> findAllByStatus(String search, int page, String status) {
        int size = 8;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        if (search == null || search.isBlank()) {
            return rentRepository.findAllByStatus(status, pageable);
        } else {

            return rentRepository.findAllByRenterNameOrBookNameAndStatus(search, status, pageable);
        }
    }

    public List<RentModel> findAllWithoutPagination(String search) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "id"));

        if (search == null || search.isBlank()) {
            return rentRepository.findAll(pageable).getContent();
        } else {
            return rentRepository.findAllBySearch(search, pageable).getContent();  
        }
    }



    public Optional<RentModel> findById(int id){
        return rentRepository.findById(id);
    }

    public ResponseEntity<Object> delivered(int id) {
        Optional<RentModel> optionalRent = rentRepository.findById(id);
        if (optionalRent.isEmpty()) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aluguel não encontrado"); }

        RentModel rent = optionalRent.get();

        rent.setDevolutionDate(LocalDate.now());

        rentValidation.deliveredValidate(id);
        rentValidation.setRentStatus(rent);

        Optional<BookModel> book = bookRepository.findById(rent.getBook().getId());
        book.get().setTotalQuantity(book.get().getTotalQuantity() + 1);
        bookRepository.save(book.get());

        rentRepository.save(rent);
        return ResponseEntity.status(HttpStatus.OK).body(rent);
    }

    public ResponseEntity<Object> update(int id, @Valid UpdateRentRecordDTO updateRentRecordDTO) {
        Optional<RentModel> rentOptional = rentRepository.findById(id);
        if (rentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aluguel não encontrado");
        }

        RentModel rentModel = rentOptional.get();

        // Verifica se o novo locatário existe
        Optional<RenterModel> newRenterOptional = renterRepository.findById(updateRentRecordDTO.renterId());
        if (newRenterOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Novo locatário não encontrado");
        }
        RenterModel newRenter = newRenterOptional.get();

        // Verifica se o novo locatário não está deletado
        if (newRenter.isDeleted()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("O locatário está deletado e não pode ser atribuído ao aluguel");
        }

        // Verifica se o novo livro existe
        Optional<BookModel> newBookOptional = bookRepository.findById(updateRentRecordDTO.bookId());
        if (newBookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Novo livro não encontrado");
        }
        BookModel newBook = newBookOptional.get();

        if (newBook.getTotalInUse() >= newBook.getTotalQuantity()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Novo livro não disponível para aluguel");
        }

        // Atualiza os dados do aluguel
        rentModel.setRenter(newRenter); // Atualiza o locatário do aluguel
        rentModel.setBook(newBook);     // Atualiza o livro do aluguel
        rentModel.setDeadLine(updateRentRecordDTO.deadLine());
        rentRepository.save(rentModel);

        // Atualiza as quantidades dos livros
        BookModel oldBook = rentModel.getBook();
        oldBook.setTotalInUse(oldBook.getTotalInUse() - 1);
        newBook.setTotalInUse(newBook.getTotalInUse() + 1);

        oldBook.setTotalQuantity(oldBook.getTotalQuantity() + 1);
        newBook.setTotalQuantity(newBook.getTotalQuantity() - 1);

        bookRepository.save(oldBook);
        bookRepository.save(newBook);

        return ResponseEntity.status(HttpStatus.OK).body("Aluguel atualizado com sucesso");
    }


}