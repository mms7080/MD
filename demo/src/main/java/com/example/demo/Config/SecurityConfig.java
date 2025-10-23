package com.example.demo.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 기본 활성 (폼에 CSRF 토큰만 넣으면 됨)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/send-code"))

            .authorizeHttpRequests(auth -> auth
             // ✅ 인증 필수 영역: 폴리오 작성/임시저장/업로드
                .requestMatchers(
                    "/folios/write/**",
                    "/folios/edit/**",
                    "/mypage/**"
                ).authenticated()

                .requestMatchers(
                    "/", "/main",
                    "/signin", "/signup",
                    "/notice/{id}", "/notice",
                    "/error",   
                    "/css/**", "/js/**", "/images/**", "/webjars/**",
                    "/uploads/**", // 이미지(훈희)
                    "/home/**", "/forgot/**",
                    "/portfolios/**"
                ).permitAll()

                // 포트폴리오 목록/상세는 열람만 공개(GET만 허용)
                .requestMatchers(
                    HttpMethod.GET, 
                    "/folios", 
                    "/folios/detail/**", 
                    "/api/folios", 
                    "/api/folios/*"
                ).permitAll()
        
                
                // 관리자만 허용
                // .requestMatchers().hasRole("ADMIN")

                // 학생만 허용
                // .requestMatchers().hasRole("STUDENT")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/home?modal=signin")              // 커스텀 로그인 페이지 (GET)
                .loginProcessingUrl("/signin") 
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/", false)     // 원래 가려던 페이지 우선
                .failureUrl("/home?modal=signin")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )

            .rememberMe(rm -> rm
                .key("SecureRandomKeyForEncryption")
                .tokenValiditySeconds(3600 * 24 * 7)
                .userDetailsService(customUserDetailsService)
            );

        return http.build();
    }
}
