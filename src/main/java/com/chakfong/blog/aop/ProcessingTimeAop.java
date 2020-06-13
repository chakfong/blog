package com.chakfong.blog.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class ProcessingTimeAop {

    @Pointcut(value = "execution(* com.chakfong.blog.web..*.*(..))")
    public void aop() {

    }

    @Around("aop()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        log.info("<=====================================================");
        log.info("请求来源： =》" + request.getRemoteAddr());
        log.info("请求URL：" + request.getRequestURL().toString());
        log.info("请求方式：" + request.getMethod());
        log.info("响应方法：" + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        log.info("请求参数：" + Arrays.toString(joinPoint.getArgs()));
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = joinPoint.proceed();
        } catch (Throwable t) {
            log.warn(t.getMessage());
            throw t;
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            log.info("[API:{}] Elapsed time: {}ms ", joinPoint.getSignature().getName(), elapsed);
            log.info("=====================================================>");
        }
        return result;
    }
}
