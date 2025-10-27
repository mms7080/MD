package com.example.demo.folio.dto;

import com.example.demo.users.UsersEntity.Users;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserInFolioDto {

    private Long id;          // PK
    private String name;      // ✅ 화면 출력용 “이름” (getName 으로 접근되도록 이름 필드명 통일)
    private String username;  // ✅ 필요하면 썸네일/타이틀 등에 활용 가능
    private String profileImage;  // ✅ Users에 프로필 이미지 필드 있으면 채워주기

    public UserInFolioDto(Users user){
        this.id = user.getId();
        this.name = user.getName();          // ✅ 필드명 userName → name 으로 변경
        this.username = user.getUsername();  // ✅ 선택적으로 추가
        this.profileImage = null;            // Users에 프로필 이미지 있으면 여기서 매핑
    }
}
