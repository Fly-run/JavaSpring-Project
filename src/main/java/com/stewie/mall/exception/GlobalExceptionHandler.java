package com.stewie.mall.exception;

import com.stewie.mall.common.BizErrorCode;
import com.stewie.mall.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。拦截所有 Controller 抛出的异常,统一转成 Result。
 * 好处:Controller 里不用写一堆 try/catch,错误格式也统一。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 参数校验失败(@Valid 触发的 @NotBlank/@NotNull 等) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(BizErrorCode.BAD_REQUEST.getMessage());
        return Result.error(BizErrorCode.BAD_REQUEST.getCode(), msg);
    }

    /** 请求体 JSON 解析失败(格式错误、编码错误等) */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return Result.error(BizErrorCode.BAD_REQUEST.getCode(), "请求体格式错误");
    }

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /** 兜底:其他未捕获异常,记录日志但不把内部细节暴露给前端 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("未捕获异常", e);
        return Result.error(BizErrorCode.SERVER_ERROR);
    }
}
