package com.company.module.esg.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * @DataJpaTest 슬라이스 테스트용 JPA Auditing 설정
 * <p>
 * core 의 JpaConfig 는 @SpringBootTest 에서만 로드되므로
 * @DataJpaTest 슬라이스 테스트에서는 별도로 Auditing 을 활성화해야 한다.
 * </p>
 */
@TestConfiguration
@EnableJpaAuditing(auditorAwareRef = "testAuditorProvider")
public class TestJpaConfig {

    @Bean
    public AuditorAware<String> testAuditorProvider() {
        return () -> Optional.of("TEST_USER");
    }
}
