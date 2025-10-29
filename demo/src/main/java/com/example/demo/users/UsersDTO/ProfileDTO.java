    package com.example.demo.users.UsersDTO;

    import java.time.LocalDate;
import java.util.Set;

import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class ProfileDTO {
        private String name;
        private String email;
        private LocalDate birth;
        private String gender;
        private String profileImgUrl;
        private String githubUrl;
        private Set<String> positions;
    }
