package com.example.demo.users.UsersRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.users.UsersEntity.DeleteStatus;
import com.example.demo.users.UsersEntity.Role;
import com.example.demo.users.UsersEntity.Users;

public interface UsersRepository extends JpaRepository<Users, Long>{
    
    // 아이디 중복 체크
    boolean existsByUsernameAndDeleteStatus(String username, DeleteStatus deleteStatus);

    // 이메일 중복 체크
    boolean existsByEmailAndDeleteStatus(String email, DeleteStatus deleteStatus);

    // 아이디로 유저 찾기
    Optional<Users> findByUsernameAndDeleteStatus(String username, DeleteStatus deleteStatus);

    // 이메일로 유저 찾기
    Optional<Users> findByEmailAndDeleteStatus(String email, DeleteStatus deleteStatus);

    // 아이디 찾기 (이메일 + 이름)
    Optional<Users> findByEmailAndNameAndDeleteStatus(String email, String name, DeleteStatus deleteStatus);


    // 비밀번호 찾기 -> 재설정 (이메일 + 아이디 + 이름)
    Optional<Users> findByEmailAndUsernameAndNameAndDeleteStatus(String email, String username, String name, DeleteStatus deleteStatus);

    // 권한별 유저 조회
    List<Users> findByRole(Role role);

    Optional<Users> findByUsername(String usernam);

}
