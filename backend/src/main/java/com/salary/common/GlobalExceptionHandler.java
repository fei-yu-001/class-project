package com.salary.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleBadCredentials(BadCredentialsException e) {
        return Result.unauthorized("用户名或密码错误");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        return Result.forbidden("没有访问权限");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.badRequest(msg);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResource(NoResourceFoundException e) {
        return Result.notFound("资源不存在");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        return Result.badRequest(e.getMessage());
    }

    /**
     * 数据库约束错误（包括 CHECK 约束、触发器 SIGNAL）
     * 例如：项目成员贡献系数之和 != 1 时由触发器抛 SQLSTATE 45000
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleDataIntegrity(org.springframework.dao.DataIntegrityViolationException e) {
        String rawMessage = e.getMostSpecificCause().getMessage();
        log.warn("数据完整性异常: {}", rawMessage);

        // 提取触发器自定义消息
        if (rawMessage != null && rawMessage.contains("ERR_PM_SUM_NOT_1")) {
            return Result.badRequest("贡献系数之和必须等于 1.00 (100%)，请调整后再保存");
        }
        if (rawMessage != null && rawMessage.contains("chk_coeff_range")) {
            return Result.badRequest("贡献系数必须在 [0, 1] 范围内");
        }
        if (rawMessage != null && rawMessage.contains("Duplicate entry")) {
            return Result.badRequest("数据已存在（重复键）");
        }
        return Result.badRequest("数据违反完整性约束: " + shortenMessage(rawMessage));
    }

    private String shortenMessage(String message) {
        if (message == null) return "未知错误";
        return message.length() > 200 ? message.substring(0, 200) + "..." : message;
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntime(RuntimeException e) {
        log.error("运行时异常: ", e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error("系统内部错误");
    }
}
