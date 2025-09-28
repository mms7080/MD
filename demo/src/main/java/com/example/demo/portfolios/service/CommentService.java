package com.example.demo.portfolios.service;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.portfolios.entity.PortfolioComment;
import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.repository.PortfolioCommentRepository;
import com.example.demo.users.UsersEntity.Role;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final PortfolioCommentRepository commentRepository; // ✅ 명확히 이름 바꾸기
    private final UsersRepository usersRepository;

    @Transactional
    public PortfolioComment addComment(Long portfolioId, String content, int rating, Principal principal) {
        Users user = usersRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        PortfoliosEntity portfolio = new PortfoliosEntity();
        portfolio.setId(portfolioId);

        PortfolioComment comment = PortfolioComment.builder()
                .portfolio(portfolio)
                .user(user)
                .content(content)
                .rating(rating)
                .build();

        return commentRepository.save(comment); // ✅ Repository 사용
    }

    public List<PortfolioComment> getComments(PortfoliosEntity portfolio) {
        return commentRepository.findByPortfolio(portfolio);
    }


    @Transactional
public void deleteComment(Long portfolioId, Long commentId, Principal principal) {
    PortfolioComment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

    // ✅ 작성자 본인인지 확인
    boolean isOwner = comment.getUser().getUsername().equals(principal.getName());

    // ✅ ADMIN 권한인지 확인
    boolean isAdmin = usersRepository.findByUsername(principal.getName())
            .map(user -> user.getRole() == Role.ADMIN)
            .orElse(false);

    if (!isOwner && !isAdmin) {
        throw new SecurityException("삭제 권한이 없습니다.");
    }

    commentRepository.delete(comment);
}

@Transactional
public void updateComment(Long portfolioId, Long commentId, String content, int rating, Principal principal) {
    PortfolioComment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

    Users currentUser = usersRepository.findByUsername(principal.getName())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    boolean isOwner = comment.getUser().getUsername().equals(currentUser.getUsername());
    boolean isAdmin = currentUser.getRole() == Role.ADMIN;

    if (!isOwner && !isAdmin) {
        throw new SecurityException("수정 권한이 없습니다.");
    }

    comment.setContent(content);
    comment.setRating(rating);
}


}

