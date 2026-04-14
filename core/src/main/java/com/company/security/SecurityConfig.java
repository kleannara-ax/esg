package com.company.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 전역 설정
 * <p>
 * core 모듈에 위치하며 업무 모듈은 이 설정을 수정하지 않는다.
 * URL 패턴 추가가 필요한 경우 SecurityPermitUrlProperties 를 통해 확장한다.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (Stateless REST API)
            .csrf(AbstractHttpConfigurer::disable)

            // 세션 비활성화 (JWT 사용)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // URL 권한 설정
            .authorizeHttpRequests(auth -> auth
                    // 인증 불필요 엔드포인트
                    .requestMatchers(HttpMethod.POST, "/user-api/auth/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/user-api/auth/signup").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    // H2 콘솔 (개발환경)
                    .requestMatchers("/h2-console/**").permitAll()
                    // Swagger
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    // 나머지는 인증 필요
                    .anyRequest().authenticated()
            )

            // H2 콘솔 프레임 허용 (개발환경)
            .headers(headers -> headers
                    .frameOptions(frame -> frame.sameOrigin()))

            // JWT 필터 등록
            .addFilterBefore(
                    new JwtAuthenticationFilter(jwtTokenProvider),
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
