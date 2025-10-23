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

    /** 목록 요약(공개용) – 기본 즉시로딩 최소화 + 필요한 것만 페치 */
    @EntityGraph(attributePaths = {"user", "skills"})
    Page<Folio> findByStatusOrderByCreatedAtDesc(Folio.Status status, Pageable pageable);

    /** 관리/전체 조회가 필요할 때 */
    @EntityGraph(attributePaths = {"user", "skills"})
    Page<Folio> findAll(Pageable pageable);

    /** 상세 조회 – 필요한 연관 전부 페치 */
    @EntityGraph(attributePaths = {"user", "skills", "photos", "projectIds"})
    Optional<Folio> findById(String id);

    /** 사용자 ID로 단건 조회 필요 시 (경로 기반 명명) */
    Optional<Folio> findByUser_Id(Long userId);

    /** 사용자 최신본 */
    Optional<Folio> findTopByUserOrderByUpdatedAtDesc(Users user);

    /** 사용자 + 상태 최신본 */
    Optional<Folio> findTopByUserAndStatusOrderByUpdatedAtDesc(Users user, Folio.Status status);

    /** 사용자 + 템플릿 최신본 */
    Optional<Folio> findTopByUserAndTemplateOrderByUpdatedAtDesc(Users user, String template);

    /** 사용자 자신의 목록(상태 필터링 포함) */
    Page<Folio> findByUserAndStatusOrderByUpdatedAtDesc(Users user, Folio.Status status, Pageable pageable);

    int countByUserAndStatus(Users user, Folio.Status status);
    List<Folio> findTop3ByUserAndStatusOrderByUpdatedAtDesc(Users user, Folio.Status status);
}