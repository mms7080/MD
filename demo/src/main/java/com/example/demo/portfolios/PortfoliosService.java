// package com.example.demo.portfolios;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// @Service
// @Transactional
// public class PortfoliosService {
    
//     private final PortfoliosRepository portfoliosRepository;
    
//     public PortfoliosService(PortfoliosRepository portfoliosRepository){
//         this.portfoliosRepository = portfoliosRepository;
//     }

//     public PortfoliosEntity getPortfolioAndIncreaseViews(Long id){
//         portfoliosRepository.increaseViewCount(id);
//         return portfoliosRepository.findById(id)
//                 .orElseThrow(()-> new RuntimeException("Portfolio not Found"));
//     }
// }
