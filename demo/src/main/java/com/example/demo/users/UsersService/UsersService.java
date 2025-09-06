package com.example.demo.users.UsersService;

import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.users.UsersDTO.UsersDTO;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersService {
    
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

   public void registerUser(UsersDTO usersDTO, String emailCode) {
        // 0) 이메일 정규화 (trim + lower)
        final String normalizedEmail = usersDTO.getEmail().trim().toLowerCase();
        String encodedPassword = passwordEncoder.encode(usersDTO.getPassword());

        // 1) 이메일 인증코드 검증(성공 시 1회성 소모)
        boolean verified = emailVerificationService.verifyCodeForSignup(normalizedEmail, emailCode);
        if (!verified) {
            throw new IllegalStateException("이메일 인증코드가 올바르지 않거나 만료되었습니다.");
        }

        // 2) 중복 검사
        Optional<Users> existing = usersRepository.findByUsername(usersDTO.getUsername());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        if (usersRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 4) 저장
        Users users = new Users();
        users.setUsername(usersDTO.getUsername());
        users.setPassword(encodedPassword);
        users.setName(usersDTO.getName());
        users.setEmail(normalizedEmail);
        usersRepository.save(users);
    }

    

}
