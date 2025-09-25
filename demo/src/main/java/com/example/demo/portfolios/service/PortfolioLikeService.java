// package com.example.demo.portfolios.service;

// import org.springframework.stereotype.Service;

// import com.example.demo.portfolios.entity.PortfolioLikesEntity;
// import com.example.demo.portfolios.entity.PortfoliosEntity;
// import com.example.demo.portfolios.repository.PortfolioLikeRepository;
// import com.example.demo.portfolios.repository.PortfoliosRepository;
// import com.example.demo.users.UsersEntity.Users;
// import com.example.demo.users.UsersRepository.UsersRepository;

// import jakarta.transaction.Transactional;
// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class PortfolioLikeService {

//     private final PortfolioLikeRepository likeRepository;
//     private final PortfoliosRepository portfolioRepository;
//     private final UsersRepository usersRepository;

//     @Transactional
//     public boolean toggleLike(Long portfolioId, String username) {
//         PortfoliosEntity portfolio = portfolioRepository.findById(portfolioId)
//                 .orElseThrow(() -> new IllegalArgumentException("포트폴리오 없음"));

//         Users user = usersRepository.findByUsername(username)
//                 .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

//         // 이미 좋아요 눌렀는지 확인
//         boolean exists = likeRepository.existsByPortfolioAndUser(portfolio, user);
//         if (exists) {
//             return false; // 이미 눌렀으면 아무것도 안 함
//         }

//         PortfolioLikesEntity like = new PortfolioLikesEntity();
//         like.setPortfolio(portfolio);
//         like.setUser(user);
//         likeRepository.save(like);

//         return true;
//     }

//     public long getLikeCount(Long portfolioId) {
//         PortfoliosEntity portfolio = portfolioRepository.findById(portfolioId)
//                 .orElseThrow(() -> new IllegalArgumentException("포트폴리오 없음"));
//         return likeRepository.countByPortfolio(portfolio);
// //     }
// }
