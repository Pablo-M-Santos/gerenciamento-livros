package com.locadora.locadoraLivro.Rents.Validation;

import com.locadora.locadoraLivro.Books.models.BookModel;
import com.locadora.locadoraLivro.Books.repositories.BookRepository;
import com.locadora.locadoraLivro.Exceptions.CustomValidationException;
import com.locadora.locadoraLivro.Renters.models.RenterModel;
import com.locadora.locadoraLivro.Renters.repositories.RenterRepository;
import com.locadora.locadoraLivro.Rents.DTOs.CreateRentRequestDTO;
import com.locadora.locadoraLivro.Rents.DTOs.UpdateRentRecordDTO;
import com.locadora.locadoraLivro.Rents.models.RentModel;
import com.locadora.locadoraLivro.Rents.models.RentStatusEnum;
import com.locadora.locadoraLivro.Rents.repositories.RentRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@Component
public class RentValidation {

    @Autowired
    RenterRepository renterRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    RentRepository rentRepository;

    public void create(CreateRentRequestDTO data) {
        validateRenterId(data);
        validateBookId(data);
        validateDeadLine(data);
        validateRentRepeated(data);
        validateRentLate(data);
    }

    public void update(UpdateRentRecordDTO data, int id) {
        validateRenterIdUpdate(data);
        validateBookIdUpdate(data);
        validateDeadLineUpdate(data, id);
    }

    public void validateRenterId(CreateRentRequestDTO data) {
        if (renterRepository.findById(data.renterId()).isEmpty()) {
            throw new CustomValidationException("Locatário não encontrado.");
        }

        RenterModel renter = renterRepository.findById(data.renterId()).get();

        if (renter.isDeleted()) {
            throw new CustomValidationException("Locatário não encontrado.");
        }
    }

    public void validateRenterIdUpdate(UpdateRentRecordDTO data) {
        if (renterRepository.findById(data.renterId()).isEmpty()) {
            throw new CustomValidationException("Locatário não encontrado.");
        }
    }

    public void validateBookId(CreateRentRequestDTO data) {
        if (bookRepository.findById(data.bookId()).isEmpty()) {
            throw new CustomValidationException("Livro não encontrado.");
        }

        BookModel book = bookRepository.findById(data.bookId()).get();

        if (book.isDeleted()) {
            throw new CustomValidationException("Livro não encontrado.");
        }
    }

    public void validateBookIdUpdate(UpdateRentRecordDTO data) {
        if (bookRepository.findById(data.bookId()).isEmpty()) {
            throw new CustomValidationException("Livro não encontrado.");
        }
    }

    public void validateDeadLine(CreateRentRequestDTO data) {
        if (data.deadLine().isAfter(LocalDate.now().plusDays(30))) {
            throw new CustomValidationException("O prazo não pode ser superior a 30 dias.");
        } else if (data.deadLine().isBefore(LocalDate.now())) {
            throw new CustomValidationException("O prazo não pode estar no passado.");
        }
    }

    public void validateDeadLineUpdate(UpdateRentRecordDTO data, int id) {
        RentModel rentPass = rentRepository.findById(id).get();
        if (!Objects.equals(rentPass.getDeadLine(), data.deadLine())) {
            if (data.deadLine().isAfter(LocalDate.now().plusDays(30))) {
                throw new CustomValidationException("O prazo não pode ser superior a 30 dias a partir de hoje.");
            } else if (data.deadLine().isBefore(LocalDate.now())) {
                throw new CustomValidationException("O prazo não pode estar no passado.");
            }
        }
    }

    public void validateBookTotalQuantity(BookModel data) {
        if (data.getTotalQuantity() <= 0) {
            throw new CustomValidationException("Não há livros disponíveis.");
        }
    }

    public void validateRentRepeated(CreateRentRequestDTO data) {
        if (rentRepository.existsByRenterIdAndBookIdAndStatus(data.renterId(), data.bookId(), RentStatusEnum.ALUGADO)) {
            throw new CustomValidationException("O locatário já alugou este livro.");
        }
    }

    public void validateRentLate(CreateRentRequestDTO data) {
        if (rentRepository.existsByRenterIdAndStatus(data.renterId(), RentStatusEnum.ATRASADO)) {
            throw new CustomValidationException("O locatário tem aluguel atrasado.");
        }
    }

    public void deliveredValidate(int id) {
        Optional<RentModel> rentOptional = rentRepository.findById(id);

        RentModel rent = rentOptional.orElseThrow(() -> new CustomValidationException("Aluguel não encontrado para o ID: " + id));

        if (rent.getStatus() == RentStatusEnum.ENTREGUE ||
                rent.getStatus() == RentStatusEnum.ENTREGUE_COM_ATRASO ||
                rent.getStatus() == RentStatusEnum.NO_PRAZO) {
            throw new CustomValidationException("O aluguel já foi devolvido.");
        }
    }


    public void setRentStatus(RentModel rent) {
        if (rent.getDevolutionDate() == null) {

            if (rent.getDeadLine().isBefore(LocalDate.now())) {
                rent.setStatus(RentStatusEnum.ATRASADO);
                rentRepository.save(rent);
            } else if (rent.getDevolutionDate() == null) {
                rent.setStatus(RentStatusEnum.ALUGADO);
                rentRepository.save(rent);
            }

        } else {

            if (rent.getDevolutionDate().isAfter(rent.getDeadLine())) {
                rent.setStatus(RentStatusEnum.ENTREGUE_COM_ATRASO);
                rentRepository.save(rent);
            } else {
                rent.setStatus(RentStatusEnum.NO_PRAZO);
                rentRepository.save(rent);
            }
        }
    }
}
