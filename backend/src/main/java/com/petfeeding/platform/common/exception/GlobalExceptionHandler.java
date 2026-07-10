package com.petfeeding.platform.common.exception;

import com.petfeeding.platform.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /** 参数校验异常 */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("参数校验失败：{}", message);
        return R.fail(400, message);
    }

    /** 参数校验异常（@RequestBody @Valid 触发） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("参数校验失败：{}", message);
        return R.fail(400, message);
    }

    /** 认证异常 */
    @ExceptionHandler(AuthenticationException.class)
    public R<Void> handleAuthException(AuthenticationException e) {
        log.warn("认证失败：{}", e.getMessage());
        return R.fail(401, "认证失败，请重新登录");
    }

    /** 权限异常 */
    @ExceptionHandler(AccessDeniedException.class)
    public R<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足：{}", e.getMessage());
        return R.fail(403, "权限不足");
    }

    /** 未知异常 */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误");
    }
}
