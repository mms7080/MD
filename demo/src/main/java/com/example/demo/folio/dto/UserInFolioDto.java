package com.example.demo.folio.dto;

import com.example.demo.users.UsersEntity.Users;
import lombok.Getter;

@Getter
public class UserInFolioDto {
    private Long id;
    private String userName;

    public UserInFolioDto(Users user){
        this.id = user.getId();
        this.userName = user.getName();
    }
}
