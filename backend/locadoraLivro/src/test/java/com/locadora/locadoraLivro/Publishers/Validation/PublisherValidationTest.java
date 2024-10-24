package com.locadora.locadoraLivro.Publishers.Validation;

import com.locadora.locadoraLivro.Publishers.DTOs.PublisherResponseDTO;
import com.locadora.locadoraLivro.Publishers.repositories.PublisherRepository;
import com.locadora.locadoraLivro.Publishers.services.PublisherServices;
import com.locadora.locadoraLivro.Users.DTOs.CreateUserRequestDTO;
import com.locadora.locadoraLivro.Users.DTOs.UpdateUserRequestDTO;
import com.locadora.locadoraLivro.Users.Validation.UserValidation;
import com.locadora.locadoraLivro.Users.models.UserModel;
import com.locadora.locadoraLivro.Users.repositories.UserRepository;
import com.locadora.locadoraLivro.Users.services.UserServices;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublisherValidationTest {

    @Mock
    private PublisherServices publisherServices;

    @Mock
    private PublisherRepository publisherRepository;

    @InjectMocks
    private PublisherValidation publisherValidation;

    private PublisherResponseDTO publisherResponseDTO;
    private UpdateUserRequestDTO updateUserRequestDTO;
    private UserModel existingUser;
}