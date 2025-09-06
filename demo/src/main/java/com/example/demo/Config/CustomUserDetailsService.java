package com.example.demo.Config;


import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UsersRepository usersRepository;

    public CustomUserDetailsService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users = usersRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 디버깅: 데이터베이스의 role 확인
        System.out.println("Database Role: " + users.getRole()); // 데이터베이스에서 가져온 역할

        // GrantedAuthority 생성
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + users.getRole().toUpperCase()));

        // CustomUserDetails 객체 반환
        return new CustomUserDetails(users, authorities);
    }
}
