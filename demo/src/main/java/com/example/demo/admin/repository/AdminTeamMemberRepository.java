package com.example.demo.admin.repository;

import com.example.demo.admin.entity.AdminTeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminTeamMemberRepository extends JpaRepository<AdminTeamMember, Long> {}
