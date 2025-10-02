package com.example.demo.portfolios.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.portfolios.entity.PortfoliosEntity;



@Repository
public interface PortfoliosRepository extends JpaRepository<PortfoliosEntity, Long> {

    @Query("SELECT DISTINCT p FROM PortfoliosEntity p " +
       "LEFT JOIN FETCH p.tags " +
       "LEFT JOIN FETCH p.screenshots " +
       "LEFT JOIN FETCH p.team " +
       "WHERE p.id = :id")
   
    Optional<PortfoliosEntity> findDetailById(@Param("id") Long id);
    Optional<PortfoliosEntity> findById(Long id);

    @Query("SELECT DISTINCT p FROM PortfoliosEntity p LEFT JOIN FETCH p.tags")
    Page<PortfoliosEntity> findAllWithTags(Pageable pageable);


    @Query("SELECT DISTINCT p FROM PortfoliosEntity p " +
    "LEFT JOIN FETCH p.team " +
    "WHERE p.id = :id")
Optional<PortfoliosEntity> findDetailByIdWithTeam(@Param("id") Long id);

@Modifying
    @Transactional
    @Query("UPDATE PortfoliosEntity p SET p.likes = p.likes + 1 WHERE p.id = :id")
    void incrementLikes(Long id);

// // ✅ 좋아요 증가
// @Modifying
// @Query("UPDATE PortfoliosEntity p SET p.likes = p.likes + 1 WHERE p.id = :id")
// int increaseLikes(@Param("id") Long id);

// // ✅ 좋아요 감소 (0 미만 안됨)
// @Modifying
// @Query("UPDATE PortfoliosEntity p SET p.likes = CASE WHEN p.likes > 0 THEN p.likes - 1 ELSE 0 END WHERE p.id = :id")
// int decreaseLikes(@Param("id") Long id);

    }
