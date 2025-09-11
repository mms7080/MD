package com.example.demo.folio.repository;

import com.example.demo.folio.entity.Folio; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolioRepository extends JpaRepository<Folio, String> {
}
