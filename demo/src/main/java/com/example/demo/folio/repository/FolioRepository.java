package com.example.demo.folio.repository;

import com.example.demo.folio.entity.Folio;
import com.example.demo.users.UsersEntity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolioRepository extends JpaRepository<Folio, String> {

    // 공개 목록(최신순)
    @EntityGraph(attributePaths = {"user", "skills"})
    Page<Folio> findByStatusOrderByCreatedAtDesc(Folio.Status status, Pageable pageable);

    // 상세
    @EntityGraph(attributePaths = {"user", "skills", "photos", "projectIds"})
    Optional<Folio> findById(String id);

    // 사용자 최신본(템플릿별)
    Optional<Folio> findTopByUserAndTemplateOrderByUpdatedAtDesc(Users user, String template);

    // 마이페이지: 카운트/최근3/페이지 목록
    long countByUserAndStatus(Users user, Folio.Status status);

    List<Folio> findTop3ByUserAndStatusOrderByUpdatedAtDesc(Users user, Folio.Status status);

    @EntityGraph(attributePaths = {"user", "skills"})
    Page<Folio> findByUserAndStatusOrderByUpdatedAtDesc(Users user, Folio.Status status, Pageable pageable);

    // (옵션) 필요 시
    @EntityGraph(attributePaths = {"user", "skills"})
    Page<Folio> findAll(Pageable pageable);

    Optional<Folio> findByUser_Id(Long userId);
}
