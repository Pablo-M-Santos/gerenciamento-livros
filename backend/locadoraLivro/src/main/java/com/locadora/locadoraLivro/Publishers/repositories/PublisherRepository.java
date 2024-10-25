package com.locadora.locadoraLivro.Publishers.repositories;

import com.locadora.locadoraLivro.Publishers.models.PublisherModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublisherRepository extends JpaRepository<PublisherModel, Integer> {
    UserDetails findByName(String name);
    PublisherModel findByNameAndIsDeletedFalse(String name);
    Page<PublisherModel> findAllByIsDeletedFalse(Pageable pageable);
    List<PublisherModel> findAllByIsDeletedFalse(Sort sort);
    PublisherModel findByEmail(String email);
    PublisherModel findBySite(String site);
    PublisherModel findByTelephone(String telephone);

    @Query("SELECT p FROM PublisherModel p WHERE LOWER(REPLACE(p.name, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%'))" +
            " OR LOWER(REPLACE(p.email, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%'))" +
            " OR LOWER(REPLACE(p.telephone, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%'))" +
            " AND p.isDeleted = false")
    Page<PublisherModel> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM PublisherModel p WHERE LOWER(REPLACE(p.name, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%'))" +
            " OR LOWER(REPLACE(p.email, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%'))" +
            " OR LOWER(REPLACE(p.telephone, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%'))" +
            " AND p.isDeleted = false")
    List<PublisherModel> findAllByKeyword(Sort sort, @Param("keyword") String keyword);
}
