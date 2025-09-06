package com.example.demo.users.UsersService;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailService emailService;

    // 설정값
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final int MAX_ATTEMPTS = 5;

    // 코드 생성용
    private static final char[] CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final SecureRandom RNG = new SecureRandom();

    /** 세션 키 prefix (이메일은 소문자 통일) */
    private String baseKey(String email) {
        return "signup:emailCode:" + email.toLowerCase(Locale.ROOT);
    }

    private String randomCode(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(CODE_CHARS[RNG.nextInt(CODE_CHARS.length)]);
        return sb.toString();
    }

    /** 인증코드 발급: 세션에 code/exp/attempts 저장 + 메일 발송 */
    public void sendCode(String email, HttpSession session) {
        // 기본 가드
        email = Objects.requireNonNull(email, "email must not be null").trim();
        if (email.isEmpty()) throw new IllegalArgumentException("email is blank");

        final String code = randomCode(6);
        final String base = baseKey(email);

        session.setAttribute(base + ":code", code);
        session.setAttribute(base + ":exp", Instant.now().plus(CODE_TTL));
        session.setAttribute(base + ":attempts", 0);

        try {
            emailService.sendVerificationCode(email, code);
        } catch (Exception e) {
            // 메일 발송 실패 시 세션에 남은 코드 정리(원치 않으면 제거)
            clearSessionKeys(session, base);
            log.error("인증 메일 전송 실패: {}", e.getMessage(), e);
            // 컨트롤러에서 사용자 친화 메시지로 처리할 수 있게 런타임 예외 전환
            throw new IllegalStateException("인증 메일 전송에 실패했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    /** 인증코드 검증(원타임 소비). 성공 시 세션에서 관련 키 제거 */
    public boolean verifyCodeForSignup(String email, String inputCode, HttpSession session) {
        if (inputCode == null || inputCode.trim().isEmpty()) return false;

        email = Objects.requireNonNull(email, "email must not be null").trim();
        if (email.isEmpty()) return false;

        final String base = baseKey(email);
        final String saved = (String) session.getAttribute(base + ":code");
        final Instant exp = (Instant) session.getAttribute(base + ":exp");
        Integer attempts = (Integer) session.getAttribute(base + ":attempts");
        if (attempts == null) attempts = 0;

        // 미발급/만료
        if (saved == null || exp == null || Instant.now().isAfter(exp)) {
            clearSessionKeys(session, base);
            return false;
        }

        // 시도 횟수 초과
        if (attempts >= MAX_ATTEMPTS) {
            clearSessionKeys(session, base);
            return false;
        }

        // 비교
        boolean ok = saved.equalsIgnoreCase(inputCode.trim());
        if (ok) {
            // 성공: 원타임 소비
            clearSessionKeys(session, base);
            // (선택) 이후 단계용 플래그
            session.setAttribute("emailVerified:" + email.toLowerCase(Locale.ROOT), true);
            return true;
        } else {
            // 실패: 시도 횟수 증가
            session.setAttribute(base + ":attempts", attempts + 1);
            return false;
        }
    }

    /** 재발급 시 기존 코드/시도/만료 정리 */
    public void invalidateCode(String email, HttpSession session) {
        if (email == null) return;
        clearSessionKeys(session, baseKey(email.trim()));
    }

    private void clearSessionKeys(HttpSession session, String base) {
        session.removeAttribute(base + ":code");
        session.removeAttribute(base + ":exp");
        session.removeAttribute(base + ":attempts");
    }
}
