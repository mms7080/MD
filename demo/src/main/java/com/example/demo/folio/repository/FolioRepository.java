package com.example.demo.folio.repository;

import com.example.demo.folio.entity.Folio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface FolioRepository extends JpaRepository<Folio, String> {

    @Override
    @EntityGraph(attributePaths = {"user", "skills"})
    Page<Folio> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "skills"})
    Optional<Folio> findById(String id);

    Optional<Folio> findByUserId(Long userId); 
}
