package com.example.demo.Config;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.users.UsersEntity.Role;
import com.example.demo.users.UsersEntity.Users;

public class CustomUserDetails implements UserDetails {
    
    private final Users users;
    private final Collection<? extends GrantedAuthority> authorities;

    // 일반 로그인용 생성자
    public CustomUserDetails(Users users, Collection<? extends GrantedAuthority> authorities) {
        this.users = users != null ? users : new Users(); // ✅ User가 없을 경우 기본 생성
        this.authorities = authorities != null ? new ArrayList<>(authorities) : new ArrayList<>();
    }

     public Users getUser() {
        return users;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return users.getPassword() != null ? users.getPassword() : "";
    }

    @Override
    public String getUsername() {
        return users.getUsername() != null ? users.getUsername() : "";
    }

    public Role getRole() {
        return users.getRole() != null ? users.getRole() : Role.USER;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
