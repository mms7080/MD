package com.example.demo.portfolios.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.portfolios.entity.PortfoliosEntity;

@Repository
public interface PortfoliosRepository extends JpaRepository<PortfoliosEntity, Long> {

    @Query("SELECT p FROM PortfoliosEntity p WHERE p.id = :id")
    Optional<PortfoliosEntity> findDetailById(@Param("id") Long id);

    /* ✅ 포트폴리오 수정 페이지 (edit.html용)
       댓글은 필요없으므로 team, screenshots만 fetch */
    @Query("SELECT DISTINCT p FROM PortfoliosEntity p " +
           "LEFT JOIN FETCH p.screenshots " +
           "LEFT JOIN FETCH p.team " +
           "WHERE p.id = :id")
    Optional<PortfoliosEntity> findDetailByIdForEdit(@Param("id") Long id);

    /* ✅ 포트폴리오 목록 (리스트 페이지용) — 태그만 fetch join */
    @Query("SELECT DISTINCT p FROM PortfoliosEntity p LEFT JOIN FETCH p.tags")
    Page<PortfoliosEntity> findAllWithTags(Pageable pageable);

    /* ✅ 좋아요 증가 */
    @Modifying
    @Transactional
    @Query("UPDATE PortfoliosEntity p SET p.likes = p.likes + 1 WHERE p.id = :id")
    int increaseLikes(@Param("id") Long id);

    /* ✅ 좋아요 감소 (0 이하 방지) */
    @Modifying
    @Transactional
    @Query("UPDATE PortfoliosEntity p SET p.likes = CASE WHEN p.likes > 0 THEN p.likes - 1 ELSE 0 END WHERE p.id = :id")
    int decreaseLikes(@Param("id") Long id);
}
