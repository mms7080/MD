package com.example.demo.users.UsersRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.users.UsersEntity.Users;

public interface UsersRepository extends JpaRepository<Users, Long>{
    
    // 아이디 중복 체크
    boolean existsByUsername(String username);

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    // 아이디로 유저 찾기
    Optional<Users> findByUsername(String username);

    // 이메일로 유저 찾기
    Optional<Users> findByEmail(String email);

    // 권한별 유저 조회
    List<Users> findByRole(String role);
}
