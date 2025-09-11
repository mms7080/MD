package com.example.demo.notice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NoticeInitConfig {

    @Bean
    CommandLineRunner initData(NoticeRepository noticeRepository) {
        return args -> {
            if (noticeRepository.count() == 0) { 
                for (int i = 1; i <= 10; i++) {
                    Notice notice = new Notice();
                    notice.setTitle("공지사항 제목 " + i);
                    notice.setContent("이것은 공지사항 내용입니다. 더미 데이터 번호 " + i);
                    notice.setWriter("관리자");
                    noticeRepository.save(notice);
                }
            }
        };
    }
}
