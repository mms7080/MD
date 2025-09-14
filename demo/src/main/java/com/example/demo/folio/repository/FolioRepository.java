package com.example.demo.folio.repository;

import com.example.demo.folio.entity.Folio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FolioRepository extends JpaRepository<Folio, String> {

    @Query("SELECT f FROM Folio f JOIN FETCH f.user JOIN FETCH f.skills")
    List<Folio> findAllWithDetails();

    @Query("SELECT f FROM Folio f JOIN FETCH f.user JOIN FETCH f.skills WHERE f.id = :id")
    Optional<Folio> findByIdWithDetails(@Param("id") String id);

    Optional<Folio> findByUserId(Long userId); 
}
