package com.eazybytes.jobportal.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

//@Aspect
//@Component
@Slf4j
public class Logging {
    @Around("execution(* com.eazybytes.jobportal..*.*(..))")
    public Object LogAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Before LogAround: {}", joinPoint.getSignature().getName());
        Object result = joinPoint.proceed();
        log.info("After LogAround: {}", joinPoint.getSignature().getName());
        return result;
    }
}
