package org.example.expert.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Aspect
@Slf4j
public class AspectAdmin {
    LocalDate now = LocalDate.now();

    @Pointcut("@annotation(org.example.expert.annotation.Admin)")
    private void adminDeleteComment() {}


    @Around("adminDeleteComment()&&args(authUser,..)")
    public Object adviceAdminDeleteComment(ProceedingJoinPoint joinPoint,AuthUser authUser) throws Throwable {
        //- **로그 기록에는 다음 정보가 포함되어야 합니다:**
        //    - 요청한 사용자의 ID
        //    - API 요청 시각
        //    - API 요청 URL
        try {
            Object result = joinPoint.proceed();
            return result;
        } finally {
            log.info("요청 사용자 id={}", authUser.getId());
            log.info("현재 시각 {}",now);
            log.info("API 요청 URL:/admin/comments/{commentId}");
        }
    }


}
