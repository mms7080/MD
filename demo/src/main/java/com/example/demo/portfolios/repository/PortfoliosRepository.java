package com.example.demo.portfolios.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    }
