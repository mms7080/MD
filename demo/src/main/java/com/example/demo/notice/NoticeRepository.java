package com.example.demo.notice;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 제목 검색
    Page<Notice> findByTitleContaining(String keyword, Pageable pageable);

    // 작성자 검색
    Page<Notice> findByWriterContaining(String keyword, Pageable pageable);

    // 이전글
    Optional<Notice> findFirstByIdLessThanOrderByIdDesc(Long id);

    //  다음글
    Optional<Notice> findFirstByIdGreaterThanOrderByIdAsc(Long id);

    // home에서 불러오기
    List<Notice> findTop4ByOrderByCreatedAtDesc();
}