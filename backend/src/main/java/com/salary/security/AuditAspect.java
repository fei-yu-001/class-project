package com.salary.security;

import com.salary.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(requireRole)")
    public Object audit(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return result;

            Integer userId = null;
            String username = null;
            if (auth.getPrincipal() instanceof Integer id) {
                userId = id;
            }
            if (auth.getDetails() instanceof String name) {
                username = name;
            }

            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            String action = className.replace("Controller", "") + "." + methodName;

            // 从参数中提取实体ID（如果有 Integer 类型参数）
            Integer entityId = null;
            String detail = null;
            for (Object arg : joinPoint.getArgs()) {
                if (arg instanceof Integer id) {
                    entityId = id;
                }
            }
            if (joinPoint.getArgs().length > 0) {
                detail = truncateArgs(joinPoint.getArgs());
            }

            auditLogService.log(userId, username, action, className, entityId, detail);
        } catch (Exception e) {
            log.warn("审计记录异常: {}", e.getMessage());
        }

        return result;
    }

    private String truncateArgs(Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            String s = String.valueOf(args[i]);
            sb.append(s.length() > 200 ? s.substring(0, 200) + "..." : s);
        }
        return sb.toString();
    }
}
