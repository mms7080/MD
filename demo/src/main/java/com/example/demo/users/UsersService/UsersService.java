package com.example.demo.users.UsersService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;

import jakarta.servlet.http.HttpSession; // ⬅️ 추가

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.users.UsersDTO.ProfileDTO;
import com.example.demo.users.UsersDTO.UsersDTO;
import com.example.demo.users.UsersEntity.DeleteStatus;
import com.example.demo.users.UsersEntity.Profile;
import com.example.demo.users.UsersEntity.Role;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.ProfileRepository;
import com.example.demo.users.UsersRepository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;
    // private final EmailVerificationService emailVerificationService;

    // 이메일 없이 회원가입
    public void registerUserWithoutEmailCode(UsersDTO usersDTO, HttpSession session) {
        if (usersDTO == null)
            throw new IllegalArgumentException("가입 정보가 없습니다.");
        if (usersDTO.getEmail() == null || usersDTO.getEmail().isBlank())
            throw new IllegalArgumentException("이메일을 입력하세요.");
        if (usersDTO.getUsername() == null || usersDTO.getUsername().isBlank())
            throw new IllegalArgumentException("아이디를 입력하세요.");
        if (usersDTO.getPassword() == null || usersDTO.getPassword().isBlank())
            throw new IllegalArgumentException("비밀번호를 입력하세요.");

        final String normalizedEmail = usersDTO.getEmail().trim().toLowerCase(Locale.ROOT);

        // 중복 검사 (아이디/이메일)
        if (usersRepository.findByUsernameAndDeleteStatus(usersDTO.getUsername(), DeleteStatus.N).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        if (usersRepository.existsByEmailAndDeleteStatus(normalizedEmail, DeleteStatus.N)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비번 해시
        String encodedPassword = passwordEncoder.encode(usersDTO.getPassword());

        // 저장
        Users users = new Users();
        users.setUsername(usersDTO.getUsername());
        users.setPassword(encodedPassword);
        users.setName(usersDTO.getName());
        users.setEmail(normalizedEmail);
        users.setAge(usersDTO.getAge());
        users.setGender(usersDTO.getGender());
        users.setRole(Role.USER);

        usersRepository.save(users);
    }

    /** 세션 기반 이메일 인증코드 검증 + 회원 저장 */
    // public void registerUser(UsersDTO usersDTO, String emailCode, HttpSession
    // session) { // ⬅️ 세션 추가
    // if (usersDTO == null) throw new IllegalArgumentException("가입 정보가 없습니다.");
    // if (usersDTO.getEmail() == null || usersDTO.getEmail().isBlank())
    // throw new IllegalArgumentException("이메일을 입력하세요.");
    // if (usersDTO.getUsername() == null || usersDTO.getUsername().isBlank())
    // throw new IllegalArgumentException("아이디를 입력하세요.");
    // if (usersDTO.getPassword() == null || usersDTO.getPassword().isBlank())
    // throw new IllegalArgumentException("비밀번호를 입력하세요.");
    // if (emailCode == null || emailCode.isBlank())
    // throw new IllegalArgumentException("인증코드를 입력하세요.");

    // // 0) 이메일 정규화
    // final String normalizedEmail =
    // usersDTO.getEmail().trim().toLowerCase(Locale.ROOT);

    // // 1) 이메일 인증코드 검증(성공 시 1회성 소모)
    // boolean verified =
    // emailVerificationService.verifyCodeForSignup(normalizedEmail, emailCode,
    // session); // ⬅️ 세션 전달
    // if (!verified) {
    // throw new IllegalStateException("이메일 인증코드가 올바르지 않거나 만료되었습니다.");
    // }

    // // 2) 중복 검사
    // if (usersRepository.findByUsername(usersDTO.getUsername()).isPresent()) {
    // throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
    // }
    // if (usersRepository.existsByEmail(normalizedEmail)) { // 가능하면 이메일도 lower 기준으로
    // 비교하도록 구현
    // throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    // }

    // // 3) 비밀번호 해시
    // String encodedPassword = passwordEncoder.encode(usersDTO.getPassword());

    // // 4) 저장
    // Users users = new Users();
    // users.setUsername(usersDTO.getUsername());
    // users.setPassword(encodedPassword);
    // users.setName(usersDTO.getName());
    // users.setEmail(normalizedEmail);
    // users.setAge(usersDTO.getAge());
    // users.setGender(usersDTO.getGender());
    // users.setRole(Role.USER);

    // usersRepository.save(users);
    // }

    // 아이디 중복 체크 (활동 중)
    public boolean isUsernameTaken(String username) {
        return usersRepository.existsByUsernameAndDeleteStatus(username, DeleteStatus.N);
    }

    // 사용자 조회 (활동 중)
    public Users getUserByUsername(String username) {
        return usersRepository.findByUsernameAndDeleteStatus(username, DeleteStatus.N)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
    }

    // 탈퇴 기능 (개인)
    public void withdrawSelf(String username, String currentRawPassword) {
        Users u = usersRepository.findByUsernameAndDeleteStatus(username, DeleteStatus.N)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(currentRawPassword, u.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        u.setDeleteStatus(DeleteStatus.Y);
        u.setDeletedAt(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public ProfileDTO loadProfileForm(Long userId) {
        Users users = usersRepository.findById(userId).orElseThrow();
        Profile profile = profileRepository.findById(userId).orElse(null);
        if (profile == null) {
            profile = new Profile();
            profile.setId(userId);
            profile.setPositions(new HashSet<>());
        }
        ProfileDTO profiledto = new ProfileDTO();
        profiledto.setName(users.getName());
        profiledto.setEmail(users.getEmail());
        profiledto.setAge(users.getAge());
        profiledto.setGender(users.getGender());
        profiledto.setProfileImgUrl(profile != null ? profile.getProfileImgUrl() : null);
        profiledto.setGithubUrl(profile != null && profile.getGithubUrl() != null ? profile.getGithubUrl() : "");
        profiledto.setPositions(profile != null && profile.getPositions() != null ? profile.getPositions() : new HashSet<>());
        return profiledto; // ✅ 반환
    }

}
