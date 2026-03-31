package com.locadora.locadoraLivro.Users.controllers;

import com.locadora.locadoraLivro.Users.DTOs.CreateUserRequestDTO;
import com.locadora.locadoraLivro.Users.DTOs.UpdateUserRequestDTO;
import com.locadora.locadoraLivro.Users.DTOs.UserResponseDTO;
import com.locadora.locadoraLivro.Users.mappers.UserMapper;
import com.locadora.locadoraLivro.Users.services.UserServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/user")
@RestController
public class UserController {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserServices userServices;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateUserRequestDTO data) {
        return userServices.create(data);
    }

    @GetMapping
    public ResponseEntity<Object> getAll(String search, @RequestParam(required = false) Integer page) {
        if (page == null) {
            return ResponseEntity.status(HttpStatus.OK).body(userMapper.toUserResponseList(userServices.findAllWithoutPagination(search)));
        }

        return ResponseEntity.status(HttpStatus.OK).body(userServices.findAll(search, page).map(userMapper::toUserResponse));
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

    @GetMapping("/count/total")
    public ResponseEntity<Long> getTotalUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(userServices.getTotalUsers());
    }

    @GetMapping("/count/admins")
    public ResponseEntity<Long> getTotalAdmins() {
        return ResponseEntity.status(HttpStatus.OK).body(userServices.getTotalAdmins());
    }

    @GetMapping("/count/locatarios")
    public ResponseEntity<Long> getTotalLocatarios() {
        return ResponseEntity.status(HttpStatus.OK).body(userServices.getTotalLocatarios());
    }
}