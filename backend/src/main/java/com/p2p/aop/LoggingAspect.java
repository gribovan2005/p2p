package com.p2p.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(public * com.p2p.service..*(..)) || execution(public * com.p2p.controller..*(..))")
    public Object logAndTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long time = System.currentTimeMillis() - start;
            logger.info("[{}] {} executed in {} ms", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), time);
            return result;
        } catch (Throwable ex) {
            logger.error("[{}] {} threw exception: {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), ex.getMessage());
            throw ex;
        }
    }
} 