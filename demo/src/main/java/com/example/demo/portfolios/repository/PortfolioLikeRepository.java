package com.example.demo.portfolios.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.portfolios.entity.PortfolioLikeEntity;
import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.users.UsersEntity.Users;

public interface PortfolioLikeRepository extends JpaRepository<PortfolioLikeEntity, Long> {
    Optional<PortfolioLikeEntity> findByPortfolioAndUser(PortfoliosEntity portfolio, Users user);
    int countByPortfolio(PortfoliosEntity portfolio);

    
}
