package com.locadora.locadoraLivro.Rents.services;

import com.locadora.locadoraLivro.Books.models.BookModel;
import com.locadora.locadoraLivro.Exceptions.ModelNotFoundException;
import com.locadora.locadoraLivro.Rents.DTOs.CreateRentRequestDTO;
import com.locadora.locadoraLivro.Rents.DTOs.UpdateRentRecordDTO;
import com.locadora.locadoraLivro.Rents.Validation.RentValidation;
import com.locadora.locadoraLivro.Rents.models.RentModel;
import com.locadora.locadoraLivro.Rents.repositories.RentRepository;
import com.locadora.locadoraLivro.Renters.models.RenterModel;
import com.locadora.locadoraLivro.Rents.repositories.BookRepository;
import com.locadora.locadoraLivro.Renters.repositories.RenterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RentServicesTest {

    @InjectMocks
    private RentServices rentServices;

    @Mock
    private RentRepository rentRepository;

    @Mock
    private RenterRepository renterRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private RentValidation rentValidation;

    private RenterModel renter;
    private BookModel book;
    private RentModel rent;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        renter = new RenterModel(1, "John Doe");
        book = new BookModel(1, "Test Book", 5);
        rent = new RentModel(renter, book, LocalDate.now().plusDays(7), LocalDate.now());
    }

    @Test
    public void testCreateRentSuccess() {
        CreateRentRequestDTO rentRequest = new CreateRentRequestDTO(1, 1, LocalDate.now().plusDays(7));

        when(renterRepository.findById(1)).thenReturn(Optional.of(renter));
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(rentRepository.save(any(RentModel.class))).thenReturn(rent);

        ResponseEntity<Void> response = rentServices.create(rentRequest);

        assertEquals(201, response.getStatusCodeValue());
        verify(rentValidation, times(1)).validateRenterId(rentRequest);
        verify(rentValidation, times(1)).validateBookId(rentRequest);
        verify(rentValidation, times(1)).validateDeadLine(rentRequest);
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    public void testFindAllRents() {
        // Adicione testes para verificar o comportamento do método findAll.
    }

    @Test
    public void testFindByIdRentFound() {
        when(rentRepository.findById(1)).thenReturn(Optional.of(rent));

        Optional<RentModel> foundRent = rentServices.findById(1);

        assertTrue(foundRent.isPresent());
        assertEquals(rent, foundRent.get());
    }

    @Test
    public void testFindByIdRentNotFound() {
        when(rentRepository.findById(1)).thenReturn(Optional.empty());

        Optional<RentModel> foundRent = rentServices.findById(1);

        assertFalse(foundRent.isPresent());
    }

    @Test
    public void testDeliveredSuccess() {
        when(rentRepository.findById(1)).thenReturn(Optional.of(rent));

        ResponseEntity<Object> response = rentServices.delivered(1);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(rent, response.getBody());
        assertNotNull(rent.getDevolutionDate());
    }

    @Test
    public void testDeliveredRentNotFound() {
        when(rentRepository.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = rentServices.delivered(1);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Rent not found", response.getBody());
    }

    @Test
    public void testUpdateRentSuccess() {
        UpdateRentRecordDTO updateDTO = new UpdateRentRecordDTO(1, 1, LocalDate.now().plusDays(10));

        when(rentRepository.findById(1)).thenReturn(Optional.of(rent));
        when(renterRepository.findById(1)).thenReturn(Optional.of(renter));
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        ResponseEntity<Object> response = rentServices.update(1, updateDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Rent updated successfully", response.getBody());
        verify(rentRepository, times(1)).save(any(RentModel.class));
    }

    @Test
    public void testUpdateRentNotFound() {
        UpdateRentRecordDTO updateDTO = new UpdateRentRecordDTO(1, 1, LocalDate.now().plusDays(10));

        when(rentRepository.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = rentServices.update(1, updateDTO);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Rent not found", response.getBody());
    }
}
