package com.company.module.esgindicator.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * 테스트용 JPA Auditing 설정
 * @DataJpaTest 슬라이스 테스트에서 @CreatedDate, @LastModifiedDate 동작을 위해 필요
 */
@TestConfiguration
@EnableJpaAuditing(auditorAwareRef = "testAuditorProvider")
public class TestJpaConfig {

    @Bean
    public AuditorAware<String> testAuditorProvider() {
        return () -> Optional.of("TEST_USER");
    }
}
