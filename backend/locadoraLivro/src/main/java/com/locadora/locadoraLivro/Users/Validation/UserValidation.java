package com.locadora.locadoraLivro.Users.Validation;

import com.locadora.locadoraLivro.Exceptions.CustomValidationException;
import com.locadora.locadoraLivro.Publishers.DTOs.CreatePublisherRequestDTO;
import com.locadora.locadoraLivro.Users.DTOs.CreateUserRequestDTO;
import com.locadora.locadoraLivro.Users.DTOs.UpdateUserRequestDTO;
import com.locadora.locadoraLivro.Users.models.UserModel;
import com.locadora.locadoraLivro.Users.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@AllArgsConstructor
@Component
public class UserValidation {

    private final UserRepository userRepository;

    private boolean isValidEmailFormat(String email) {
        return email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    public void create (CreateUserRequestDTO data) {
        validateName(data);
        validateEmail(data);
    }

    public void update (UpdateUserRequestDTO data, int id) {
        validateNameUpdate(data, id);
        validateUpdateEmail(data, id);
    }

    public void validateName(CreateUserRequestDTO data){
        if (data.name() == null || data.name().isEmpty()) {
            throw new CustomValidationException("O nome de usuário não pode estar vazio.");
        }
        if (userRepository.findByName(data.name()) != null) {
            throw new CustomValidationException("Nome de usuário já em uso");
        }
    }

    public void validateNameUpdate(UpdateUserRequestDTO data, int id){
        UserModel userModel = userRepository.findById(id).get();

        if (!Objects.equals(userModel.getName(), data.name())){
            if (userRepository.findByName(data.name()) != null){
                throw new CustomValidationException("Nome de usuário já em uso");
            }
        }
    }

    public void validateEmail(CreateUserRequestDTO data) {
        if (data.email() == null || data.email().isEmpty()) {
            throw new CustomValidationException("O e-mail não pode estar vazio.");
        }
        if (!isValidEmailFormat(data.email())) {
            throw new CustomValidationException("Formato de e-mail inválido.");
        }
        if (userRepository.findByEmail(data.email()) != null) {
            throw new CustomValidationException("E-mail já em uso");
        }
    }

    public void validateUpdateEmail(UpdateUserRequestDTO data, int id) {
        UserModel userModel = userRepository.findById(id).get();

        if (!Objects.equals(userModel.getEmail(), data.email())){
            if (userRepository.findByEmail(data.email()) != null) {
                throw new CustomValidationException("E-mail já em uso");
            }
        }
    }
}