package com.example.demo.portfolios.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.portfolios.entity.PortfolioComment;
import com.example.demo.portfolios.entity.PortfoliosEntity;

public interface PortfolioCommentRepository extends JpaRepository<PortfolioComment, Long> {
    List<PortfolioComment> findByPortfolio(PortfoliosEntity portfolio);
    List<PortfolioComment> findByPortfolioIdOrderByCreatedAtDesc(Long portfolioId);

    @Query("SELECT c FROM PortfolioComment c JOIN FETCH c.user WHERE c.portfolio.id = :portfolioId ORDER BY c.createdAt DESC")
List<PortfolioComment> findByPortfolioIdWithUser(@Param("portfolioId") Long portfolioId);

}

    
