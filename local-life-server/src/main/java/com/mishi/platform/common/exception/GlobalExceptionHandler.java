package com.mishi.platform.common.exception;

import com.mishi.platform.common.constant.ErrorCode;
import com.mishi.platform.common.result.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器 — HTTP 状态码映射
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e, HttpServletResponse response) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b).orElse("参数校验失败");
        response.setStatus(400);
        return Result.error(ErrorCode.PARAM_INVALID, msg);
    }

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletResponse response) {
        log.warn("业务异常: {}", e.getMessage());
        // 根据 code 映射 HTTP 状态码
        int status = switch (e.getCode() != null ? e.getCode() : 0) {
            case 401 -> 401;
            case 403 -> 403;
            case 404 -> 404;
            default -> 400;
        };
        response.setStatus(status);
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e, HttpServletResponse response) {
        log.error("系统异常: ", e);
        response.setStatus(500);
        return Result.error(500, "服务器内部错误");
    }
}
