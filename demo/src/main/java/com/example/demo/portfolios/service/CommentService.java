package com.example.demo.portfolios.service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.portfolios.entity.PortfolioComment;
import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.repository.PortfolioCommentRepository;
import com.example.demo.portfolios.repository.PortfoliosRepository;
import com.example.demo.users.UsersEntity.Role;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final PortfolioCommentRepository commentRepository; // ✅ 명확히 이름 바꾸기
    private final UsersRepository usersRepository;
    private final PortfoliosRepository portfoliosRepository; // ✅ 추가

 @Transactional
public PortfolioComment addComment(Long portfolioId, String content, int rating, Principal principal) {
    // ✅ 로그인 사용자 가져오기
    Users user = usersRepository.findByUsername(principal.getName())
            .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

    // ✅ 포트폴리오 가져오기 (쿼리 최적화 위해 getReferenceById 사용 가능)
    PortfoliosEntity portfolio = portfoliosRepository.getReferenceById(portfolioId);

    // ✅ 간단한 유효성 검사
    if (content == null || content.trim().isEmpty()) {
        throw new IllegalArgumentException("댓글 내용을 입력해야 합니다.");
    }
    if (rating < 1 || rating > 5) {
        throw new IllegalArgumentException("별점은 1~5 사이여야 합니다.");
    }

    // ✅ 댓글 엔티티 생성
    PortfolioComment comment = PortfolioComment.builder()
            .portfolio(portfolio)
            .user(user)
            .content(content.trim())
            .rating(rating)
            .createdAt(LocalDateTime.now()) // Auditing 대신 직접 세팅도 가능
            .build();

    // ✅ 저장 후 반환
    return commentRepository.save(comment);
}


public List<PortfolioComment> getComments(Long portfolioId) {
    return commentRepository.findByPortfolioIdWithUser(portfolioId);
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


@Transactional(readOnly = true)
public Page<PortfolioComment> getComments(Long portfolioId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    return commentRepository.findByPortfolioId(portfolioId, pageable);
}

public double getAverageRating(Long portfolioId) {
    return commentRepository.findAverageRatingByPortfolioId(portfolioId);
}

public long getCommentCount(Long portfolioId) {
    return commentRepository.countByPortfolioId(portfolioId);
}


}

