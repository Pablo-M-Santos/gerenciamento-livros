package com.locadora.locadoraLivro.Users.repositories;

import com.locadora.locadoraLivro.Users.models.UserModel;
import com.locadora.locadoraLivro.Users.models.UserRoleEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Integer> {
    UserDetails findByName(String name);
    UserModel findByEmail(String email);

    @Query("SELECT u FROM UserModel u WHERE LOWER(REPLACE(u.name, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%'))" +
            " OR LOWER(REPLACE(u.email, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%'))" +
            " OR u.role = :role")
    Page<UserModel> findAllByKeywordOrRole(@Param("keyword") String keyword, @Param("role") UserRoleEnum role, Pageable pageable);

    @Query("SELECT u FROM UserModel u WHERE LOWER(REPLACE(u.name, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%'))" +
            " OR LOWER(REPLACE(u.email, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%'))")
    List<UserModel> findAllByKeyword(@Param("keyword") String keyword, Sort sort);
}

