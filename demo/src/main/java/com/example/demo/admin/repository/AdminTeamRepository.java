package com.example.demo.admin.repository;

import com.example.demo.admin.entity.AdminTeam;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminTeamRepository extends JpaRepository<AdminTeam, Long> {

    @Query("""
        select distinct t
        from AdminTeam t
        left join fetch t.members
        where t.id in (
            select t2.id
            from AdminTeam t2
            join t2.members m2
            where m2.userId = :userId
        )
        """)
    List<AdminTeam> findWithAllMembersByMemberUserId(@Param("userId") Long userId);
}
