package com.locadora.locadoraLivro.Renters.Validation;

import com.locadora.locadoraLivro.Exceptions.CustomValidationException;
import com.locadora.locadoraLivro.Renters.DTOs.CreateRenterRequestDTO;
import com.locadora.locadoraLivro.Renters.DTOs.UpdateRenterRequestDTO;
import com.locadora.locadoraLivro.Renters.models.RenterModel;
import com.locadora.locadoraLivro.Renters.repositories.RenterRepository;
import com.locadora.locadoraLivro.Renters.services.RenterServices;
import com.locadora.locadoraLivro.Rents.repositories.RentRepository;
import lombok.AllArgsConstructor;
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.regex.Pattern;

@AllArgsConstructor
@Component
public class RenterValidation {

    @Autowired
    private RenterRepository renterRepository;

    @Autowired
    private RentRepository rentRepository;

    @Autowired
    private RenterServices renterServices;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\(\\d{2}\\) \\d{5}-\\d{4}$");

    public void create(CreateRenterRequestDTO data) {
        validateName(data);
        validateEmail(data);
        validateTelephone(data);
        validateAddress(data);
        validateCPF(data);
    }

    public void update(UpdateRenterRequestDTO data, int id) {
        validateNameUpdate(data, id);
        validateUpdateEmail(data, id);
        validateTelephoneUpdate(data, id);
        validateAddressUpdate(data, id);
        validateCPFUpdate(data, id);
    }

    public void delete(int id) {
        validateDeleteRenter(id);
    }

    private void validateName(CreateRenterRequestDTO data) {
        if (data.name() == null || data.name().isBlank()) {
            throw new CustomValidationException("Nome não pode ser vazio ou nulo.");
        }
    }

    private void validateNameUpdate(UpdateRenterRequestDTO data, int id) {
        if (data.name() == null || data.name().isBlank()) {
            throw new CustomValidationException("Nome não pode ser vazio ou nulo.");
        }
    }

    private void validateEmail(CreateRenterRequestDTO data) {
        if (data.email() == null || data.email().isBlank()) {
            throw new CustomValidationException("E-mail não pode ser vazio ou nulo.");
        }
        if (!EMAIL_PATTERN.matcher(data.email()).matches()) {
            throw new CustomValidationException("Formato de e-mail inválido.");
        }
        if (renterRepository.findByEmailAndIsDeletedFalse(data.email()) != null) {
            throw new CustomValidationException("E-mail já em uso.");
        }
    }

    private void validateUpdateEmail(UpdateRenterRequestDTO data, int id) {
        RenterModel renter = renterRepository.findById(id).orElseThrow(() ->
                new CustomValidationException("Locatário não encontrado.")
        );

        if (data.email() == null || data.email().isBlank()) {
            throw new CustomValidationException("E-mail não pode ser vazio ou nulo.");
        }
        if (!EMAIL_PATTERN.matcher(data.email()).matches()) {
            throw new CustomValidationException("Formato de e-mail inválido.");
        }
        if (!Objects.equals(renter.getEmail(), data.email())) {
            if (renterRepository.findByEmailAndIsDeletedFalse(data.email()) != null) {
                throw new CustomValidationException("E-mail já em uso.");
            }
        }
    }

    private void validateTelephone(CreateRenterRequestDTO data) {
        if (data.telephone() == null || data.telephone().isBlank()) {
            throw new CustomValidationException("Telefone não pode ser vazio ou nulo.");
        }
        if (!PHONE_PATTERN.matcher(data.telephone()).matches()) {
            throw new CustomValidationException("Formato de telefone inválido. Use o formato (XX) XXXXX-XXXX.");
        }
    }

    private void validateTelephoneUpdate(UpdateRenterRequestDTO data, int id) {
        RenterModel renter = renterRepository.findById(id).orElseThrow(() ->
                new CustomValidationException("Locatário não encontrado.")
        );

        if (data.telephone() == null || data.telephone().isBlank()) {
            throw new CustomValidationException("Telefone não pode ser vazio ou nulo.");
        }
        if (!PHONE_PATTERN.matcher(data.telephone()).matches()) {
            throw new CustomValidationException("Formato de telefone inválido. Use o formato (XX) XXXXX-XXXX.");
        }
    }

    private void validateAddress(CreateRenterRequestDTO data) {
        if (data.address() == null || data.address().isBlank()) {
            throw new CustomValidationException("Endereço não pode ser vazio ou nulo.");
        }
    }

    private void validateAddressUpdate(UpdateRenterRequestDTO data, int id) {
        RenterModel renter = renterRepository.findById(id).orElseThrow(() ->
                new CustomValidationException("Locatário não encontrado.")
        );

        if (data.address() == null || data.address().isBlank()) {
            throw new CustomValidationException("Endereço não pode ser vazio ou nulo.");
        }
    }

    private void validateCPF(CreateRenterRequestDTO data) {
        if (data.cpf() == null || data.cpf().isBlank()) {
            throw new CustomValidationException("CPF não pode ser vazio ou nulo.");
        }

        CPFValidator cpfValidator = new CPFValidator();
        cpfValidator.initialize(null);

        if (!cpfValidator.isValid(data.cpf(), null)) {
            throw new CustomValidationException("Formato de CPF inválido.");
        }

        if (renterRepository.findByCpfAndIsDeletedFalse(data.cpf()) != null) {
            throw new CustomValidationException("CPF já em uso.");
        }
    }

    private void validateCPFUpdate(UpdateRenterRequestDTO data, int id) {
        RenterModel renter = renterRepository.findById(id).orElseThrow(() ->
                new CustomValidationException("Locatário não encontrado.")
        );

        // Verifique se o CPF está presente e não é nulo
        if (data.cpf() != null && !data.cpf().isBlank()) {
            CPFValidator cpfValidator = new CPFValidator();
            cpfValidator.initialize(null);

            // Verifica se o CPF é válido
            if (!cpfValidator.isValid(data.cpf(), null)) {
                throw new CustomValidationException("Formato de CPF inválido.");
            }

            // Se o CPF é diferente do existente e já está em uso
            if (!Objects.equals(renter.getCpf(), data.cpf()) &&
                    renterRepository.findByCpfAndIsDeletedFalse(data.cpf()) != null) {
                throw new CustomValidationException("CPF já em uso.");
            }
        }
    }





    public void validateDeleteRenter(int id) {
        if (renterServices.hasRentedBooks(id)) {
            throw new CustomValidationException("Não é possível excluir o locatário. Existem livros atualmente alugados.");
        }
    }

}