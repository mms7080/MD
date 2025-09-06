package com.example.demo.users.UsersDTO;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsersDTO {

    public interface Create {}
    public interface Update {}

    @NotBlank(message = "아이디는 필수 입력 항목입니다.", groups = Create.class)
    @Pattern(regexp = "^[a-z0-9]{4,16}$", message = "아이디는 영문 소문자와 숫자로 4~16자이어야 합니다.", groups = Create.class)
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.", groups = Create.class)
    @Size(min = 10, message = "비밀번호는 최소 10자 이상이어야 합니다.", groups = Create.class)
    private String password; 

    private String confirmPassword;

    @NotBlank(message = "이름이 작성되지 않았습니다.", groups = Create.class)
    private String name;

    @NotBlank(message = "이메일은 필수입니다.", groups = Create.class)
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email; 
}
