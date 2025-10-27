package com.example.demo.portfolios.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.demo.portfolios.entity.PortfoliosEntity;

@Repository
public interface PortfoliosRepository extends JpaRepository<PortfoliosEntity, Long> {

    // ✅ 상세 페이지용 — 태그, 팀, 스크린샷, 댓글, 좋아요까지 미리 로드
    // 수정 ✅
    @Query("""
        SELECT DISTINCT p FROM PortfoliosEntity p
        LEFT JOIN FETCH p.tags
        LEFT JOIN FETCH p.team
        LEFT JOIN FETCH p.likes
        LEFT JOIN FETCH p.comments c
        LEFT JOIN FETCH c.user
        WHERE p.id = :id
    """)
    Optional<PortfoliosEntity> findDetailById(@Param("id") Long id);
    

    @EntityGraph(attributePaths = {"tags", "screenshots", "team"}) // ❌ comments, likes 제외
@Query("SELECT p FROM PortfoliosEntity p WHERE p.id = :id")
Optional<PortfoliosEntity> findDetailByIdForEdit(@Param("id") Long id);

    // ✅ 리스트 페이지용 — likes, tags, team 까지 미리 로드해서 Lazy 문제 해결
    @EntityGraph(attributePaths = {"tags", "team", "likes"})
    @Query(
        value = "SELECT p FROM PortfoliosEntity p ORDER BY p.createdAt DESC",
        countQuery = "SELECT COUNT(p) FROM PortfoliosEntity p"
    )
    Page<PortfoliosEntity> findAllBasic(Pageable pageable);


     // ✅ 수정 시 최소 정보만 불러오는 버전 (screenshots/team/tags만)
     @EntityGraph(attributePaths = {"screenshots", "team"})
     @Query("SELECT p FROM PortfoliosEntity p WHERE p.id = :id")
     Optional<PortfoliosEntity> findForUpdate(@Param("id") Long id);



     @Query("""
    SELECT DISTINCT p FROM PortfoliosEntity p
    LEFT JOIN p.tags t
    WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(p.desc) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
Page<PortfoliosEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);




// 제목만 검색
@Query("""
    SELECT DISTINCT p FROM PortfoliosEntity p
    WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
Page<PortfoliosEntity> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

// 태그 필터링
@Query("""
    SELECT DISTINCT p FROM PortfoliosEntity p
    JOIN p.tags t
    WHERE t IN :tags
""")
Page<PortfoliosEntity> searchByTags(@Param("tags") List<String> tags, Pageable pageable);

// 제목 + 태그 조합
@Query("""
    SELECT DISTINCT p FROM PortfoliosEntity p
    JOIN p.tags t
    WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
    AND t IN :tags
""")
Page<PortfoliosEntity> searchByTitleAndTags(@Param("keyword") String keyword,
                                            @Param("tags") List<String> tags,
                                            Pageable pageable);




@Query("""
    SELECT DISTINCT p 
    FROM PortfoliosEntity p 
    LEFT JOIN FETCH p.screenshots 
    WHERE p.isPublic = true 
    ORDER BY p.id DESC
    """)
    List<PortfoliosEntity> findAllWithScreenshots();
                                                
                                            

                                            




                                        }
