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
        // 提取用户信息（操作前，确保即使操作失败也能记录）
        Integer userId = null;
        String username = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                if (auth.getPrincipal() instanceof Integer id) {
                    userId = id;
                }
                username = auth.getName();
            }
        } catch (Exception ignored) {}

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String action = className.replace("Controller", "") + "." + methodName;

        // 提取第一个 Integer 参数作为实体ID
        Integer entityId = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Integer id) {
                entityId = id;
                break; // 取第一个，不覆盖
            }
        }

        String detail = null;
        if (joinPoint.getArgs().length > 0) {
            detail = truncateArgs(joinPoint.getArgs());
        }

        Object result;
        String outcome = "成功";
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            outcome = "失败: " + ex.getMessage();
            // 记录失败操作后重新抛出异常
            try {
                auditLogService.log(userId, username, action + " [" + outcome + "]",
                        className, entityId, detail);
            } catch (Exception logEx) {
                log.warn("审计记录异常: {}", logEx.getMessage());
            }
            throw ex;
        }

        // 记录成功操作（异步，不阻塞）
        try {
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
