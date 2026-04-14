package com.company.module.esg.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @WebMvcTest 슬라이스 테스트용 Security 설정
 * <p>
 * core 의 SecurityConfig 대신 사용하여 테스트를 단순화한다.
 * - @WithMockUser 가 정상 동작하도록 Security 를 활성화 유지
 * - JWT 필터를 제거하여 불필요한 복잡도 제거
 * </p>
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                    .anyRequest().authenticated()
            );
        return http.build();
    }
}
