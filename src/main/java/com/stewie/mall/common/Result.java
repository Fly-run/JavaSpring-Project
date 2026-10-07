package com.stewie.mall.common;

import lombok.Data;

/**
 * 统一返回体。所有接口都返回 Result,前端/调用方统一处理。
 * code:200 成功;401 未登录;500 服务器错误等。
 */
@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> Result<T> error(BizErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }
}
