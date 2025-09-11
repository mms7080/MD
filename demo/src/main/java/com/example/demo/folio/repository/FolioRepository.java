package com.example.demo.folio.repository;

import com.example.demo.folio.entity.Folio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FolioRepository extends JpaRepository<Folio, String> {

    @Query("SELECT f FROM Folio f JOIN FETCH f.user JOIN FETCH f.skills")
    List<Folio> findAllWithDetails();
}
