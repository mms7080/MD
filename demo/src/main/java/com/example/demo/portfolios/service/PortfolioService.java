package com.example.demo.portfolios.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.repository.PortfoliosRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    
    private final PortfoliosRepository portfoliosRepository;

    public PortfoliosEntity save(PortfoliosEntity portfolios){
        return portfoliosRepository.save(portfolios);
    }

}
