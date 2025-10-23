    package com.example.demo.users.UsersDTO;

    import java.util.Set;

import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class ProfileDTO {
        private String name;
        private String email;
        private Integer age;
        private String gender;
        private String profileImgUrl;
        private String githubUrl;
        private Set<String> positions;
    }
