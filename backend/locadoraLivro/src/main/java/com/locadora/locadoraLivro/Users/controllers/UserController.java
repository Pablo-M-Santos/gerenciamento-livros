package com.locadora.locadoraLivro.Users.controllers;

import com.locadora.locadoraLivro.Users.DTOs.CreateUserRequestDTO;
import com.locadora.locadoraLivro.Users.DTOs.UpdateUserRequestDTO;
import com.locadora.locadoraLivro.Users.DTOs.UserResponseDTO;
import com.locadora.locadoraLivro.Users.mappers.UserMapper;
import com.locadora.locadoraLivro.Users.models.UserModel;
import com.locadora.locadoraLivro.Users.models.UserRoleEnum;
import com.locadora.locadoraLivro.Users.services.UserServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserServices userServices;

    @PostMapping
    public ResponseEntity<UserModel> create(@RequestBody @Valid CreateUserRequestDTO data) {
        return userServices.create(data);
    }

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestParam(required = false) String search, @RequestParam(required = false) UserRoleEnum role, @RequestParam(required = false) Integer page) {
        if (page == null) {
            return ResponseEntity.status(HttpStatus.OK).body(userMapper.toUserResponseList(userServices.findAllWithoutPagination(search, role)));
        }

        return ResponseEntity.status(HttpStatus.OK).body(userServices.findAll(search, role, page).map(userMapper::toUserResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable(value = "id") int id) {
        return ResponseEntity.status(HttpStatus.OK).body(userMapper.toUserResponse(userServices.findById(id).get()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable(value = "id") int id, @RequestBody @Valid UpdateUserRequestDTO updateUserRequestDTO) {
        return userServices.update(id, updateUserRequestDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable(value = "id") int id) {
        return userServices.delete(id);
    }
}