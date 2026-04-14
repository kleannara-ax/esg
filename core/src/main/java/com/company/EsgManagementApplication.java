package com.company;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ESG 관리시스템 메인 애플리케이션
 * <p>
 * scanBasePackages 를 "com.company" 로 설정하여
 * 하위 모든 업무 모듈(module-xxx)의 컴포넌트를 자동 탐지한다.
 * </p>
 */
@SpringBootApplication(scanBasePackages = "com.company")
public class EsgManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsgManagementApplication.class, args);
    }
}
