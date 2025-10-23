package com.example.demo.users.UsersRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.users.UsersEntity.Profile;


public interface ProfileRepository extends JpaRepository<Profile, Long>{

}
