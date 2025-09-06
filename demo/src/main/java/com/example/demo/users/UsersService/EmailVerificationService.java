package com.example.demo.users.UsersService;

import java.security.SecureRandom;
import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final StringRedisTemplate redis;
    private final EmailService emailService;

     private static final Duration CODE_TTL = Duration.ofMinutes(5);

    private static final char[] CODE_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final SecureRandom RNG = new SecureRandom();

    private String kCode(String email){ return "auth:code:signup:" + email.toLowerCase(); }

    private String randomCode(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(CODE_CHARS[RNG.nextInt(CODE_CHARS.length)]);
        }
        return sb.toString();
    }

    public void sendCode(String email){
        String code = randomCode(6);
        redis.opsForValue().set(kCode(email), code, CODE_TTL);
        emailService.sendVerificationCode(email, code);
    }

    public boolean verifyCodeForSignup(String email, String inputCode){
        ValueOperations<String,String> ops = redis.opsForValue();
        String saved = ops.get(kCode(email));
        if (saved == null || inputCode == null) return false;

        boolean ok = saved.equalsIgnoreCase(inputCode.trim());
        if (ok) {
            redis.delete(kCode(email));
        }
        return ok;
    }
    
}
