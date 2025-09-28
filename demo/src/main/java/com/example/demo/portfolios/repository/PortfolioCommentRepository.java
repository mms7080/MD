package com.example.demo.portfolios.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.portfolios.entity.PortfolioComment;
import com.example.demo.portfolios.entity.PortfoliosEntity;

public interface PortfolioCommentRepository extends JpaRepository<PortfolioComment, Long> {
    List<PortfolioComment> findByPortfolio(PortfoliosEntity portfolio);
}

    
