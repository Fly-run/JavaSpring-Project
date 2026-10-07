package com.stewie.mall.exception;

import com.stewie.mall.common.BizErrorCode;
import lombok.Getter;

/**
 * 业务异常。业务代码里主动抛出,由全局异常处理器统一转成 Result 返回。
 * 例:throw new BusinessException(BizErrorCode.NOT_FOUND);
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(BizErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(String message) {
        this(BizErrorCode.SERVER_ERROR.getCode(), message);
    }
}
