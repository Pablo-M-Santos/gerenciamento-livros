package com.locadora.locadoraLivro.Users.Validation;

import com.locadora.locadoraLivro.Exceptions.CustomValidationException;
import com.locadora.locadoraLivro.Renters.DTOs.CreateRenterRequestDTO;
import com.locadora.locadoraLivro.Users.DTOs.CreateUserRequestDTO;
import com.locadora.locadoraLivro.Users.DTOs.UpdateUserRequestDTO;
import com.locadora.locadoraLivro.Users.models.UserModel;
import com.locadora.locadoraLivro.Users.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@Component
public class UserValidation {

    private final UserRepository userRepository;

    public void create(CreateUserRequestDTO data){
        validateName(data);
        validateEmail(data);
    }
    public void update(UpdateUserRequestDTO data, int id){
        validateNameUpdate(data, id);
        validateUpdateEmail(data, id);
    }

    // Validações de Nome
    public void validateName(CreateUserRequestDTO data) {
        if (data.name() == null || data.name().trim().isEmpty()) {
            throw new CustomValidationException("O nome de usuário não pode estar vazio.");
        }

        if (userRepository.findByName(data.name()) != null) {
            throw new CustomValidationException("Nome de usuário já em uso.");
        }
    }

    public void validateNameUpdate(UpdateUserRequestDTO data, int id) {
        Optional<UserModel> userOptional = userRepository.findById(id);

        if (userOptional.isEmpty()) {
            throw new CustomValidationException("Usuário não encontrado.");
        }

        UserModel userModel = userOptional.get();

        if (!Objects.equals(userModel.getName(), data.name())) {
            if (userRepository.findByName(data.name()) != null) {
                throw new CustomValidationException("Nome de usuário já em uso.");
            }
        }
    }

    // Validações de Email
    public void validateEmail(CreateUserRequestDTO data) {
        if (data.email() == null || data.email().trim().isEmpty()) {
            throw new CustomValidationException("O e-mail não pode estar vazio.");
        }

        if (!data.email().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new CustomValidationException("Formato de e-mail inválido.");
        }

        if (userRepository.findByEmail(data.email()) != null) {
            throw new CustomValidationException("E-mail já em uso.");
        }
    }

    public void validateUpdateEmail(UpdateUserRequestDTO data, int id) {
        Optional<UserModel> userOptional = userRepository.findById(id);

        if (userOptional.isEmpty()) {
            throw new CustomValidationException("Usuário não encontrado.");
        }

        UserModel userModel = userOptional.get();

        if (!Objects.equals(userModel.getEmail(), data.email())) {
            if (userRepository.findByEmail(data.email()) != null) {
                throw new CustomValidationException("E-mail já em uso.");
            }
        }
    }

    public void validatePassword(CreateUserRequestDTO data) {
        if (data.password() == null || data.password().trim().isEmpty()) {
            throw new CustomValidationException("Senha não pode ser nula ou vazia");
        }
    }

}
