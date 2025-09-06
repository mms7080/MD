package com.example.demo.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
            .csrf(csrf -> { })

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/main",
                    "/signin",
                    "/signup",
                    "/error",   
                    "/send-code",
                    "/css/**", "/js/**", "/images/**", "/webjars/**"
                ).permitAll()
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/signin")              // 커스텀 로그인 페이지 (GET)
                .loginProcessingUrl("/api/signin") // 로그인 처리 (POST) — Spring Security가 가로채 처리
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/", false)     // 원래 가려던 페이지 우선
                .failureUrl("/signin?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/signin?logout")
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
