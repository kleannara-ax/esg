package com.company.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * 서비스/컨트롤러 계층 로깅 AOP
 * <p>
 * - 모든 업무 모듈(com.company.module.*)의 Service / Controller 실행 시간을 자동으로 기록한다.
 * - core 에 위치하므로 업무 모듈에서 별도 설정 없이 자동 적용된다.
 * </p>
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /** 모든 업무 모듈 서비스 */
    @Pointcut("execution(* com.company.module..service..*(..))")
    public void serviceLayer() {}

    /** 모든 업무 모듈 컨트롤러 */
    @Pointcut("execution(* com.company.module..controller..*(..))")
    public void controllerLayer() {}

    @Around("serviceLayer()")
    public Object logService(ProceedingJoinPoint pjp) throws Throwable {
        return logExecution(pjp, "SERVICE");
    }

    @Around("controllerLayer()")
    public Object logController(ProceedingJoinPoint pjp) throws Throwable {
        return logExecution(pjp, "CONTROLLER");
    }

    private Object logExecution(ProceedingJoinPoint pjp, String layer) throws Throwable {
        String signature = pjp.getSignature().toShortString();
        StopWatch stopWatch = new StopWatch();

        log.debug("[{}] START → {}", layer, signature);
        stopWatch.start();
        try {
            Object result = pjp.proceed();
            stopWatch.stop();
            log.debug("[{}] END   → {} ({}ms)", layer, signature, stopWatch.getTotalTimeMillis());
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            log.error("[{}] ERROR → {} ({}ms) : {}", layer, signature,
                    stopWatch.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }
}
