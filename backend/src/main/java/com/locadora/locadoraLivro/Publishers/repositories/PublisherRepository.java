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
import java.util.Optional;

@Repository
public interface PublisherRepository extends JpaRepository<PublisherModel, Integer> {
    UserDetails findByName(String name);
    PublisherModel findByNameAndIsDeletedFalse(String name);
    Page<PublisherModel> findAllByIsDeletedFalse(Pageable pageable);
    List<PublisherModel> findAllByIsDeletedFalse(Sort sort);
    PublisherModel findByEmail(String email);
    PublisherModel findByEmailAndIsDeletedFalse(String email);
    PublisherModel findBySite(String site);
    PublisherModel findBySiteAndIsDeletedFalse(String site);
    PublisherModel findByTelephone(String telephone);
    PublisherModel findByTelephoneAndIsDeletedFalse(String telephone);
    Optional<PublisherModel> findByNameOrEmailAndIsDeletedTrue(String name, String email);

    @Query("SELECT u FROM PublisherModel u WHERE " +
            "(LOWER(REPLACE(u.name, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:searchTerm, ' ', ''), '%')) " +
            "OR LOWER(REPLACE(u.email, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:searchTerm, ' ', ''), '%')) " +
            "OR LOWER(REPLACE(REPLACE(REPLACE(REPLACE(u.telephone, '(', ''), ')', ''), '-', ''), ' ', '')) " +
            "LIKE LOWER(CONCAT('%', REPLACE(REPLACE(REPLACE(REPLACE(:searchTerm, '(', ''), ')', ''), '-', ''), ' ', ''), '%'))) " +
            "AND u.isDeleted = false")
    List<PublisherModel> findAllBySearchTerm(@Param("searchTerm") String searchTerm, Sort sort);

    @Query("SELECT u FROM PublisherModel u WHERE " +
            "(LOWER(REPLACE(u.name, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:searchTerm, ' ', ''), '%')) " +
            "OR LOWER(REPLACE(u.email, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:searchTerm, ' ', ''), '%')) " +
            "OR LOWER(REPLACE(REPLACE(REPLACE(REPLACE(u.telephone, '(', ''), ')', ''), '-', ''), ' ', '')) " +
            "LIKE LOWER(CONCAT('%', REPLACE(REPLACE(REPLACE(REPLACE(:searchTerm, '(', ''), ')', ''), '-', ''), ' ', ''), '%'))) " +
            "AND u.isDeleted = false")
    Page<PublisherModel> findAllBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(u) FROM PublisherModel u WHERE u.isDeleted = false")
    long countActivePublishers();

    @Query("SELECT COUNT(u) FROM PublisherModel u WHERE u.isDeleted = true")
    long countDeletedPublishers();

    @Query("SELECT COUNT(u) FROM PublisherModel u")
    long countTotalPublishers();
}
