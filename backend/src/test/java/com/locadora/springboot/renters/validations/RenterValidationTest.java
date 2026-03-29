package com.locadora.springboot.renters.validations;


import com.locadora.locadoraLivro.Exceptions.CustomValidationException;
import com.locadora.locadoraLivro.Renters.DTOs.CreateRenterRequestDTO;
import com.locadora.locadoraLivro.Renters.DTOs.UpdateRenterRequestDTO;
import com.locadora.locadoraLivro.Renters.Validation.RenterValidation;
import com.locadora.locadoraLivro.Renters.models.RenterModel;
import com.locadora.locadoraLivro.Renters.repositories.RenterRepository;
import com.locadora.locadoraLivro.Rents.repositories.RentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RenterValidationTest {

    @Mock
    RenterRepository renterRepository;

    @Mock
    RentRepository rentRepository;

    @InjectMocks
    RenterValidation renterValidation;

    private CreateRenterRequestDTO createRenterRequestDTO;
    private CreateRenterRequestDTO renterWithCpfInvalid;
    private UpdateRenterRequestDTO updateRenterRequestDTO;
    private UpdateRenterRequestDTO updateRenterWithCpfInvalid;
    private RenterModel existingRenter;

    @BeforeEach
    void setUp() {
        createRenterRequestDTO = new CreateRenterRequestDTO("Renter", "renter@gmail.com", "(85)980028922", "Rua tal", "000.000.000-00");
        renterWithCpfInvalid = new CreateRenterRequestDTO("Renter", "renter@gmail.com", "(85)980028922", "Rua tal", "000.030.000-00");
        updateRenterRequestDTO = new UpdateRenterRequestDTO("Renter", "renterupdate@gmail.com", "(85)980028922", "Rua tal", "856.543.750-75");
        updateRenterWithCpfInvalid = new UpdateRenterRequestDTO("Renter", "renterupdate@gmail.com", "(85)980028922", "Rua tal", "222.222.222-75");
        existingRenter = new RenterModel(1, "Renter", "renter@gmail.com", "(85)980028922", "Rua tal", "000.000.000-00", false);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInUse() {
        when(renterRepository.findByEmailAndIsDeletedFalse(createRenterRequestDTO.email())).thenReturn(existingRenter);

        assertThrows(CustomValidationException.class, () -> renterValidation.create(createRenterRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenCpfIsInUse() {
        when(renterRepository.findByCpfAndIsDeletedFalse(createRenterRequestDTO.cpf())).thenReturn(existingRenter);

        assertThrows(CustomValidationException.class, () -> renterValidation.create(createRenterRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenCpfIsInvalid() {
        assertThrows(CustomValidationException.class, () -> renterValidation.create(renterWithCpfInvalid));
    }

    @Test
    void shouldThrowExceptionWhenNameIsNullOrEmpty() {
        CreateRenterRequestDTO nullNameDTO = new CreateRenterRequestDTO(null, "john@gmail.com", "(85)980028922", "Rua tal", "000.000.000-00");
        assertThrows(CustomValidationException.class, () -> renterValidation.create(nullNameDTO), "O nome não pode estar vazio.");

        CreateRenterRequestDTO emptyNameDTO = new CreateRenterRequestDTO("", "john@gmail.com", "(85)980028922", "Rua tal", "000.000.000-00");
        assertThrows(CustomValidationException.class, () -> renterValidation.create(emptyNameDTO), "O nome não pode estar vazio.");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNullOrEmpty() {
        CreateRenterRequestDTO nullNameDTO = new CreateRenterRequestDTO("Renter", null, "(85)980028922", "Rua tal", "000.000.000-00");
        assertThrows(CustomValidationException.class, () -> renterValidation.create(nullNameDTO), "O email não pode estar vazio.");

        CreateRenterRequestDTO emptyNameDTO = new CreateRenterRequestDTO("Renter", "", "(85)980028922", "Rua tal", "000.000.000-00");
        assertThrows(CustomValidationException.class, () -> renterValidation.create(emptyNameDTO), "O email não pode estar vazio.");
    }

    @Test
    void shouldCreateWhenValidData() {
        when(renterRepository.findByEmailAndIsDeletedFalse(createRenterRequestDTO.email())).thenReturn(null);
        when(renterRepository.findByCpfAndIsDeletedFalse(createRenterRequestDTO.cpf())).thenReturn(null);

        renterValidation.create(createRenterRequestDTO);
    }

    @Test
    void shouldThrowExceptionUpdatingWithExistingEmail() {
        when(renterRepository.findById(existingRenter.getId())).thenReturn(Optional.ofNullable(existingRenter));
        when(renterRepository.findByEmailAndIsDeletedFalse(updateRenterRequestDTO.email())).thenReturn(existingRenter);

        assertThrows(CustomValidationException.class, () -> renterValidation.update(updateRenterRequestDTO, existingRenter.getId()));
    }

    @Test
    void shouldThrowExceptionUpdatingWithExistingCpf() {
        when(renterRepository.findById(existingRenter.getId())).thenReturn(Optional.ofNullable(existingRenter));
        when(renterRepository.findByCpfAndIsDeletedFalse(updateRenterRequestDTO.cpf())).thenReturn(existingRenter);

        assertThrows(CustomValidationException.class, () -> renterValidation.update(updateRenterRequestDTO, existingRenter.getId()));
    }

    @Test
    void shouldThrowExceptionUpdatingWithInvalidCpf() {
        when(renterRepository.findById(existingRenter.getId())).thenReturn(Optional.ofNullable(existingRenter));

        assertThrows(CustomValidationException.class, () -> renterValidation.update(updateRenterWithCpfInvalid, existingRenter.getId()));
    }

    @Test
    void shouldThrowExceptionUpdatingWhenNameIsNullOrEmpty() {
        UpdateRenterRequestDTO nullNameDTO = new UpdateRenterRequestDTO(null, "john@gmail.com", "(85)980028922", "Rua tal", "000.000.000-00");
        assertThrows(CustomValidationException.class, () -> renterValidation.update(nullNameDTO, existingRenter.getId()), "O nome não pode estar vazio.");

        UpdateRenterRequestDTO emptyNameDTO = new UpdateRenterRequestDTO("", "john@gmail.com", "(85)980028922", "Rua tal", "000.000.000-00");
        assertThrows(CustomValidationException.class, () -> renterValidation.update(emptyNameDTO, existingRenter.getId()), "O nome não pode estar vazio.");
    }

    @Test
    void shouldThrowExceptionUpdatingWhenEmailIsNullOrEmpty() {
        CreateRenterRequestDTO nullNameDTO = new CreateRenterRequestDTO("Renter", null, "(85)980028922", "Rua tal", "000.000.000-00");
        assertThrows(CustomValidationException.class, () -> renterValidation.create(nullNameDTO), "O email não pode estar vazio.");

        CreateRenterRequestDTO emptyNameDTO = new CreateRenterRequestDTO("Renter", "", "(85)980028922", "Rua tal", "000.000.000-00");
        assertThrows(CustomValidationException.class, () -> renterValidation.create(emptyNameDTO), "O email não pode estar vazio.");
    }

    @Test
    void shouldUpdateWithValidData() {
        when(renterRepository.findById(existingRenter.getId())).thenReturn(Optional.ofNullable(existingRenter));
        when(renterRepository.findByCpfAndIsDeletedFalse(updateRenterRequestDTO.cpf())).thenReturn(null);
        when(renterRepository.findByEmailAndIsDeletedFalse(updateRenterRequestDTO.email())).thenReturn(null);

        renterValidation.update(updateRenterRequestDTO, existingRenter.getId());
    }

}