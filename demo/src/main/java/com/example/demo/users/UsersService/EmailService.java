package com.example.demo.users.UsersService;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    
      public void sendVerificationCode(String to, String code) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject("[Kafolio] 이메일 인증 코드");
            String html = """
                <h2>이메일 인증</h2>
                <p>아래 인증코드를 입력하세요. (유효 5분)</p>
                <div style="font-size:28px;font-weight:700;letter-spacing:6px">%s</div>
            """.formatted(code);
            helper.setText(html, true);
            helper.setFrom(new InternetAddress("${spring.mail.username}", "Kafolio", "UTF-8"));

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new IllegalStateException("메일 전송 실패", e);
        }
    }
}
