package com.example.demo.portfolios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.portfolios.entity.PortfoliosEntity;

@Repository
public interface PortfoliosRepository extends JpaRepository<PortfoliosEntity, Long> {

}
