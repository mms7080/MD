package com.example.demo.folio.repository;

import com.example.demo.folio.entity.Folio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

@Repository
public interface FolioRepository extends JpaRepository<Folio, String> {

     @EntityGraph(attributePaths = {"user", "skills"})
    Page<Folio> findAll(Pageable pageable);

    // 상세 조회 시 필요한 모든 연관 데이터를 즉시 로딩하도록 변경
    @EntityGraph(attributePaths = {"user", "skills", "photos", "projectIds"})
    Optional<Folio> findById(String id);

    Optional<Folio> findByUserId(Long userId);
}