// package com.example.demo.portfolios;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Modifying;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// public interface PortfoliosRepository extends JpaRepository<PortfoliosEntity, Long> {
    
//     @Modifying
//     @Query("UPDATE PortfoliosEntity p SET p.viewCount = p.viewCount +1 WHERE p.id = :id")
//     void increaseViewCount(@Param("id") Long id);
// }
