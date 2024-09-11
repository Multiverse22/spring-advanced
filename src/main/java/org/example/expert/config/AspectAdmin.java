package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Aspect
@Slf4j
public class AspectAdmin {
    LocalDateTime now = LocalDateTime.now();

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
            log.info("요청 사용자 id={}", authUser.getId()); //- 요청한 사용자의 ID
            log.info("현재 시각 {}",now); //- API 요청 시각
            HttpServletRequest request =
                    ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            log.info(request.getMethod()+" : "+request.getServletPath()); //- API 요청 URL 와 HTTP Method
        }
    }

}
