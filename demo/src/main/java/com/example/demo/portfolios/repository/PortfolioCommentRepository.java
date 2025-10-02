package com.example.demo.portfolios.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

Page<PortfolioComment> findByPortfolioId(Long portfolioId, Pageable pageable);

@Query(
  value = "SELECT c FROM PortfolioComment c JOIN FETCH c.user WHERE c.portfolio.id = :portfolioId ORDER BY c.createdAt DESC",
  countQuery = "SELECT COUNT(c) FROM PortfolioComment c WHERE c.portfolio.id = :portfolioId"
)
Page<PortfolioComment> findByPortfolioIdWithUser(@Param("portfolioId") Long portfolioId, Pageable pageable);



 // 전체 평균 별점 구하기
 @Query("SELECT COALESCE(AVG(c.rating), 0) FROM PortfolioComment c WHERE c.portfolio.id = :portfolioId")
 double findAverageRatingByPortfolioId(@Param("portfolioId") Long portfolioId);

 // 전체 댓글 개수 (Page.getTotalElements() 대신 써도 됨)
 long countByPortfolioId(Long portfolioId);

}

    
